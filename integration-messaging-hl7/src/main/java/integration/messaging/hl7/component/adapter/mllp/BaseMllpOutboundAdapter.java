package integration.messaging.hl7.component.adapter.mllp;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.dto.MessageFlowStepDto;
import integration.messaging.component.BaseMessagingComponent;
import integration.messaging.component.MessageConsumer;
import integration.messaging.component.MessageProducer;
import integration.messaging.component.adapter.BaseOutboundAdapter;

/**
 * Base class for all MLLP/HL7 Outbound communication points.
 */
public abstract class BaseMllpOutboundAdapter extends BaseOutboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMllpOutboundAdapter.class);
    
    private static final String CONTENT_TYPE = "HL7";
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();

    public BaseMllpOutboundAdapter(String componentName) {
        super(componentName);
    }

    @Override
    public void addMessageProducer(MessageProducer messageProducer) {
        if (!messageProducers.contains(messageProducer)) {
            this.messageProducers.add(messageProducer);
            messageProducer.addMessageConsumer(this);
        }
    }
    
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @Override
    protected void setDefaultURIOptions() {
        addURIOption("sync", "true");
        addURIOption("encoders", "#hl7encoder");
        addURIOption("decoders", "#hl7decoder");  
    }

    
    public String getTargetHost() {
        return componentProperties.get("TARGET_HOST");
    }

    
    public String getTargetPort() {
        return componentProperties.get("TARGET_PORT");
    }

    
    @Override
    public String getToUriString() {
        String target = getTargetHost() + ":" + getTargetPort();
        return "netty:tcp://" + target + constructOptions();
    }
    
    
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();
        
        // A route to process outbound message handling complete events.  This is the final stage of an inbound adapter.
        from("direct:handleOutboundMessageHandlingCompleteEvent-" + identifier.getComponentPath())
            .routeId("handleOutboundMessageHandlingCompleteEvent-" + identifier.getComponentPath())
            .routeGroup(identifier.getComponentPath())
            .transacted()
                .process(new Processor() {
    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Delete the event.
                        Long eventId = exchange.getMessage().getBody(Long.class);
                        messagingFlowService.deleteEvent(eventId);
                        
                        // Set the message flow step id as the exchange message body so it can be added to the queue.
                        Long messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_STEP_ID);
                        MessageFlowStepDto messageFlowStepDto = messagingFlowService.retrieveMessageFlow(messageFlowId);
                        
                        exchange.getMessage().setBody(messageFlowStepDto.getMessageContent());   
                    }
                })
                .to(getToUriString());
    }
}
