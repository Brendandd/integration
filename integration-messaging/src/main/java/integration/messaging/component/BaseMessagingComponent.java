package integration.messaging.component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.ComponentDto;
import integration.core.dto.ComponentRouteDto;
import integration.core.dto.MessageFlowEventDto;
import integration.core.dto.RouteDto;
import integration.core.exception.ConfigurationException;
import integration.core.service.ConfigurationService;
import integration.messaging.ComponentIdentifier;
import integration.messaging.service.MessagingFlowService;
import jakarta.annotation.PostConstruct;

/**
 * Base class for all Apache Camel messaging component routes.
 * 
 * All messaging components are implemented as multiple Apache Camel routes. All components are separated into inbound message handling and outbound message handling with a JMS queue allowing communication between
 * the two.  
 * 
 * Communication between different message components is done via JMS topics.  
 * 
 * Messaging components can be adapters (inbound and outbound), message handlers (eg. transformers, filters, splitters) and route connectors (inbound and outbound).
 * 
 * To ensure guaranteed message delivery between components the transactional outbox pattern is used.  Firstly a message is written to an event table within the same transactions as the 
 * message flow record is stored in the main table.  A timer process then processes these events and within the same transaction the event is removed and a message written to a JMS topic to be 
 * picked up by another component.  
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseMessagingComponent extends RouteBuilder implements Component {   
    public static final String MESSAGE_FLOW_STEP_ID = "messageFlowStepId";
    
    protected String componentName;
    
    @Autowired
    protected CamelContext camelContext;

    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    
    @Autowired
    protected Ignite ignite;
    
    @Autowired
    protected ConfigurationService configurationService;

    @Autowired
    protected ComponentIdentifier identifier;

    protected boolean isInboundRunning;
    protected boolean isOutboundRunning;

    protected Map<String, String> componentProperties;

    @Autowired
    protected MessagingFlowService messagingFlowService;

    @Autowired
    protected ProducerTemplate producerTemplate;
    
    @Autowired
    private RouteConfigLoader configLoader;

    
    public BaseMessagingComponent(String componentName) {
        this.componentName = componentName;
    }

    
    @PostConstruct
    public void init() {
        identifier.setComponentName(componentName);
    }

    
    /**
     * The content type handled by this component.
     * 
     * @return
     */
    public abstract String getContentType();

    
    @Override
    public ComponentIdentifier getIdentifier() {
        return identifier;
    }
    
    
    public abstract Logger getLogger();

    
    @Override
    public void setRoute(String route) {
        identifier.setRouteName(route);
        
        // Get the route
        RouteDto routeDto = configurationService.getRouteByName(getIdentifier().getRouteName());

        if (routeDto == null) {
            throw new ConfigurationException("Route not found. Route name: " + getIdentifier().getRouteName());
        }

        // Get the component
        ComponentDto componentDto = configurationService.getComponentByName(getIdentifier().getComponentName());

        if (componentDto == null) {
            throw new ConfigurationException("Component not found. Component name: " + getIdentifier().getComponentName());
        }

        // Now get the component route object. This will throw an exception if the
        // component is not on this route.
        ComponentRouteDto componentRouteDto = configurationService.getComponentRoute(componentDto.getId(), routeDto.getId());

        getIdentifier().setComponentRouteId(componentRouteDto.getId());
        getIdentifier().setRouteId(routeDto.getId());
        getIdentifier().setComponentId(componentDto.getId());

        
        // Read the config for the component       
        componentProperties = configLoader.getConfiguration(routeDto.getName(), componentDto.getName());

        // Now we need to read the component state from the database to see if it should
        // be started on startup.
        isInboundRunning = configurationService.isInboundRunning(componentRouteDto.getId());
        isOutboundRunning = configurationService.isOutboundRunning(componentRouteDto.getId());
    }

    
    @Override
    public void configure() throws Exception {
        
        // A route to add the message flow step id to the inbound message handling complete queue.
        from("direct:addToInboundMessageHandlingCompleteQueue-" + identifier.getComponentPath())
            .routeId("addToInboundMessageHandlingCompleteQueue-" + identifier.getComponentPath())
            .routeGroup(identifier.getComponentPath())
            .transacted()
                .process(new Processor() {
    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Delete the event.
                        Long eventId =  exchange.getMessage().getBody(Long.class);
                        messagingFlowService.deleteEvent(eventId);
                        
                        // Set the message flow step id as the exchange message body so it can be added to the queue.
                        Long messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_STEP_ID);
                        exchange.getMessage().setBody(messageFlowId);
                    }
                })
            .to("jms:queue:inboundMessageHandlingComplete-" + identifier.getComponentPath());

        
        
        // A route which reads from the components internal message handling complete queue.  This is the entry point for a components outbound message handling.
        from("jms:queue:inboundMessageHandlingComplete-" + identifier.getComponentPath() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
            .routeId("inboundMessageHandlingComplete-" + identifier.getComponentPath())
            .autoStartup(isOutboundRunning)
            .routeGroup(identifier.getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .transacted()
           // All components must provide an outboudMessageHandling route.
           .to("direct:outboundMessageHandling-" + identifier.getComponentPath());
    }

    
    /**
     * A timer to process messages which have completed inbound message handling.  These message get added to a queue so the components outbound message handler can do further message handling.
     */
    @Scheduled(fixedRate = 100)
    public void processComponentInboundMessageHandlingCompleteEvents() {
        if (!camelContext.isStarted()) {
            return;
        }
        
        IgniteCache<String, Integer> cache = ignite.getOrCreateCache("eventCache3");

        List<MessageFlowEventDto> events = null;

        Lock lock = cache.lock(MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE + "-" + identifier.getComponentPath());

        try {
            // Acquire the lock
            lock.lock();

            events = messagingFlowService.getEvents(identifier, 20,MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE);

            // Each event read we add to the queue and then delete the event.
            for (MessageFlowEventDto event : events) {
                long messageFlowId = event.getMessageFlowId();

                producerTemplate.sendBodyAndHeader("direct:addToInboundMessageHandlingCompleteQueue-" + identifier.getComponentPath(),event.getId(), BaseMessagingComponent.MESSAGE_FLOW_STEP_ID, messageFlowId);
            }
        } finally {
            // Release the lock
            lock.unlock();
        }
    }

    
    /**
     * A timer to process messages which have completed inbound message handling.  These message get added to a queue so the components outbound message handler can do further message handling.
     */
    @Scheduled(fixedRate = 100)
    public void processComponentOutboundMessageHandlingCompleteEvents() {
        if (!camelContext.isStarted()) {
            return;
        }
        
        IgniteCache<String, Integer> cache = ignite.getOrCreateCache("eventCache3");

        List<MessageFlowEventDto> events = null;

        Lock lock = cache.lock(MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE + "-" + identifier.getComponentPath());

        try {
            // Acquire the lock
            lock.lock();

            events = messagingFlowService.getEvents(identifier, 20,MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE);

            // Each event read we add to the queue and then delete the event.
            for (MessageFlowEventDto event : events) {
                long messageFlowId = event.getMessageFlowId();
                
                // All components needs to provide a route handleOutboundMessageHandlingCompleteEvent
                producerTemplate.sendBodyAndHeader("direct:handleOutboundMessageHandlingCompleteEvent-" + identifier.getComponentPath(),event.getId(), BaseMessagingComponent.MESSAGE_FLOW_STEP_ID, messageFlowId);
            }
        } finally {
            // Release the lock
            lock.unlock();
        }
    }
}
