package integration.core.messaging.component;

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
import org.springframework.core.env.Environment;

import integration.core.domain.configuration.ComponentState;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.ComponentDto;
import integration.core.dto.MessageFlowDto;
import integration.core.dto.MessageFlowEventDto;
import integration.core.exception.ConfigurationException;
import integration.core.exception.EventProcessingException;
import integration.core.messaging.BaseRoute;
import integration.core.service.ConfigurationService;
import integration.core.service.MessageFlowPropertyService;
import integration.core.service.MessagingFlowService;

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
public abstract class BaseMessagingComponent extends RouteBuilder implements MessagingComponent {   
    public static final String MESSAGE_FLOW_ID = "messageFlowId";
    public static final String EVENT_ID = "eventId";
    
    protected long identifier;
    protected BaseRoute route;
    protected String owner;
    
    protected ComponentState inboundState;
    protected ComponentState outboundState;
    
    @Autowired
    protected ConfigurationService configurationService;
    
    @Autowired
    protected CamelContext camelContext;

    
    @Autowired
    protected MessagingFlowService messagingFlowService;
    
    protected Map<String,String>componentProperties;
    
    @Autowired
    private Environment env;
    
    @Autowired
    protected ProducerTemplate producerTemplate;
    
    @Autowired
    protected MessageFlowPropertyService messageFlowPropertyService;
    
    /**
     * The content type handled by this component.
     * 
     * @return
     */
    public abstract String getContentType();

    
    public abstract Logger getLogger();
    
    @Autowired
    protected Ignite ignite;

    
    /**
     * Where to forward the message too.  This is a Camel uri. 
     * 
     * @return
     * @throws ConfigurationException 
     */
    public abstract String getMessageForwardingUriString() throws ConfigurationException;

    
    /**
     * The full component path.  owner-route-path
     */
    @Override
    public String getComponentPath() throws ConfigurationException {
        return getOwner() + "-" + route.getName() + "-" + getName();
    }

    
    @Override
    public long getIdentifier() {
        return identifier;
    }

    
    @Override
    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    
    @Override
    public void configure() throws Exception {
        
        // Sets the event as failed.
        onException(EventProcessingException.class)
        .process(exchange -> {
            Long eventId = exchange.getIn().getHeader(BaseMessagingComponent.EVENT_ID, Long.class);
            messagingFlowService.setEventFailed(eventId);
        });
        
     
        // A route to add the message flow step id to the inbound message handling complete queue.
        from("direct:addToInboundMessageHandlingCompleteQueue-" + getComponentPath())
            .routeId("addToInboundMessageHandlingCompleteQueue-" + getComponentPath())
            .routeGroup(getComponentPath())  
            .transacted()
                .process(new Processor() {
    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Long eventId = null;
                        Long messageFlowId = null;
                        
                        try {
                            // Delete the event.
                            eventId = exchange.getMessage().getBody(Long.class);
                            messagingFlowService.deleteEvent(eventId);
                            
                            // Get the message flow step id.
                            messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID);
    
                            // Add the message to the queue
                            producerTemplate.sendBody("jms:queue:inboundMessageHandlingComplete-" + getComponentPath(), messageFlowId);
                        } catch (Exception e) {
                            throw new EventProcessingException("Error adding the message flow id to the queue", eventId, messageFlowId, getComponentPath(), e);
                        }
                    }
                });

        
        // A route which reads from the components internal message handling complete queue.  This is the entry point for a components outbound message handling.
        from("jms:queue:inboundMessageHandlingComplete-" + getComponentPath() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
            .routeId("outboundEntryPoint-" + getComponentPath())
            .routeGroup(getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .transacted()            
                // All components must provide an outboudMessageHandling route.
                .to("direct:outboundMessageHandling-" + getComponentPath());

        
        
        // Event processor routes.
        from("timer://eventProcessorTimer-" + getComponentPath() + "?fixedRate=true&period=100&delay=2000")
        .routeId("eventProcessorTimer-" + getComponentPath())
        .process(exchange -> {
            String owner = env.getProperty("owner");
            IgniteCache<String, Integer> cache = ignite.getOrCreateCache("eventCache3");
            Lock lock = cache.lock(getComponentPath());

            lock.lock(); // Lock acquired

            try {
                List<MessageFlowEventDto> events = messagingFlowService.getEventsForComponent(owner, 400, getComponentPath());

                for (MessageFlowEventDto event : events) {
                    Exchange subExchange = exchange.copy();

                    subExchange.getIn().setHeader(BaseMessagingComponent.MESSAGE_FLOW_ID, event.getMessageFlowId());
                    subExchange.getIn().setHeader("eventId", event.getId());
                    subExchange.getIn().setBody(event.getId());
                    
                    String uri;
                    if (event.getType() == MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE) {
                        uri = "addToInboundMessageHandlingCompleteQueue" + "-" + event.getComponentPath();
                    } else if (event.getType() == MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE) {
                        uri = "handleOutboundMessageHandlingCompleteEvent" + "-" + event.getComponentPath();
                    } else {
                        continue; // skip unknown types
                    }

                    exchange.getContext().createProducerTemplate().send("direct:" + uri, subExchange);
                }

            } finally {
                lock.unlock(); // Release lock after all events processed
            }
        });
    
    
        // Timer to check the state of a component and take the appropriate action eg. stop, start or do nothing.
        from("timer://stateTimer-" + getComponentPath() + "?fixedRate=true&period=100&delay=2000")
        .routeId("stateTimer-" + getComponentPath())
        .process(exchange -> {
            ComponentDto component = configurationService.getComponent(identifier);
                
            // Process inbound state change
            if (component.getInboundState() != inboundState) {
                if (component.getInboundState() == ComponentState.RUNNING) {
                    camelContext.getRouteController().startRoute("inboundEntryPoint-" + getComponentPath());
                } else {
                    camelContext.getRouteController().stopRoute("inboundEntryPoint-" + getComponentPath());
                }
                
                inboundState = component.getInboundState();
            }
            
            // Process outbound state change
            if (component.getOutboundState() != outboundState) {
                if (component.getOutboundState() == ComponentState.RUNNING) {
                    camelContext.getRouteController().startRoute("outboundExitPoint-" + getComponentPath());
                } else {
                    camelContext.getRouteController().stopRoute("outboundExitPoint-" + getComponentPath());
                }
                
                outboundState = component.getOutboundState();
            }
        });

        
        // A route to process outbound message handling complete events.  This is the final stage of a components processing.
        from("direct:handleOutboundMessageHandlingCompleteEvent-" + getComponentPath())
            .routeId("outboundExitPoint-" + getComponentPath())
            .routeGroup(getComponentPath())
            .autoStartup(outboundState == ComponentState.RUNNING)
            .transacted()
            .process(new Processor() {

                @Override
                public void process(Exchange exchange) throws Exception { 
                    Long eventId = null;
                    Long messageFlowId = null;
                    
                    try {
                        // Delete the event.
                        eventId = (long)exchange.getMessage().getHeader(BaseMessagingComponent.EVENT_ID);
                        messagingFlowService.deleteEvent(eventId);
                    
                        // Record a message forwarded step.
                        messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID);
                        MessageFlowDto messageFlowDto = messagingFlowService.recordMessageFlow(BaseMessagingComponent.this, messageFlowId, MessageFlowActionType.FORWARDED);
                        
                        // Do any pre forwarding processing.  Subclasses can provide their own.  The default is no processing.
                        preForwardingProcessing(exchange);
                        
                        // Get the appropriate body content to send out.  Subclasses need to provide the content.
                        producerTemplate.sendBody(getMessageForwardingUriString(), getBodyContent(messageFlowDto));
                    } catch(Exception e) {
                        throw new EventProcessingException("Error adding message step flow id to the topic", eventId, messageFlowId, getComponentPath(), e);
                    }
                }
            });
     }

    
    /**
     * Custom processing required by a component before forwarding the message eg. set headers.
     * 
     * @param exchange
     */
    protected void preForwardingProcessing(Exchange exchange) {
        // The default is nothing.
    }

    
    /**
     * Get the body content that needs to be forwarded to the next component/send externally.
     * 
     * @param messageFlowDto
     * @return
     */
    protected abstract Object getBodyContent(MessageFlowDto messageFlowDto);
    
    
    @Override
    public BaseRoute getRoute() {
        return route;
    }


    @Override
    public void setRoute(BaseRoute route) {
        this.route = route;
    }


    @Override
    public Map<String, String> getConfiguration() {
        return componentProperties;
    }
    
    
    @Override
    public void setConfiguration(Map<String, String> componentProperties) {
        this.componentProperties = componentProperties; 
    }

    
    @Override
    public ComponentState getInboundState() {
        return inboundState;
    }


    @Override
    public void setInboundState(ComponentState inboundState) {
        this.inboundState = inboundState;
    }


    @Override
    public ComponentState getOutboundState() {
        return outboundState;
    }


    @Override
    public void setOutboundState(ComponentState outboundState) {
        this.outboundState = outboundState;
    }


    @Override
    public String getOwner() {
        return env.getProperty("owner");
    }

    
    /**
     * Returns the component name which has been defined in the @IntegrationComponent annotation.
     */
    @Override
    public String getName() throws ConfigurationException {
        IntegrationComponent annotation = this.getClass().getAnnotation(IntegrationComponent.class);
        
        if (annotation == null) {
            throw new ConfigurationException("@IntegrationComponent annotation not found.  It is mandatory for all components");
        }
        
        return annotation.name();
    }
}
