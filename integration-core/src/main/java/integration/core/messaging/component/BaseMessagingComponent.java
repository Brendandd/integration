package integration.core.messaging.component;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import integration.core.messaging.BaseRoute;
import integration.core.service.ConfigurationService;
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
    public static final String MESSAGE_FLOW_STEP_ID = "messageFlowStepId";
    
    protected long identifier;
    protected BaseRoute route;
    protected String owner;
    
    @Autowired
    protected CamelContext camelContext;
    
    @Autowired
    protected Ignite ignite;
    
    @Autowired
    protected ConfigurationService configurationService;

    protected boolean isInboundRunning;
    protected boolean isOutboundRunning;

    @Autowired
    protected MessagingFlowService messagingFlowService;

    @Autowired
    protected ProducerTemplate producerTemplate;
    
    protected Map<String,String>componentProperties;
    
    @Autowired
    private Environment env;
    
    
    /**
     * The content type handled by this component.
     * 
     * @return
     */
    public abstract String getContentType();

    
    public abstract Logger getLogger();
    
    
    /**
     * The full component path.  owner-route-path
     */
    @Override
    public String getComponentPath() {
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
        
        // A route to add the message flow step id to the inbound message handling complete queue.
        from("direct:addToInboundMessageHandlingCompleteQueue-" + getComponentPath())
            .routeId("addToInboundMessageHandlingCompleteQueue-" + getComponentPath())
            .routeGroup(getComponentPath())
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
            .to("jms:queue:inboundMessageHandlingComplete-" + getComponentPath());

        
        
        // A route which reads from the components internal message handling complete queue.  This is the entry point for a components outbound message handling.
        from("jms:queue:inboundMessageHandlingComplete-" + getComponentPath() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
            .routeId("inboundMessageHandlingComplete-" + getComponentPath())
            .autoStartup(isOutboundRunning)
            .routeGroup(getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .transacted()
           // All components must provide an outboudMessageHandling route.
           .to("direct:outboundMessageHandling-" + getComponentPath());
    }

    
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
    public boolean isInboundRunning() {
        return isInboundRunning;
    }


    @Override
    public boolean isOutboundRunning() {
        return isOutboundRunning;
    }


    @Override
    public void setInboundRunning(boolean isRunning) {
        this.isInboundRunning = isRunning;
        
    }


    @Override
    public void setOutboundRunning(boolean isRunning) {
        this.isOutboundRunning = isRunning;
    }

    
    @Override
    public String getOwner() {
        return env.getProperty("owner");
    }
}
