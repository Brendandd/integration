package integration.messaging.hl7.component.adapter.mllp;

import static org.apache.camel.component.hl7.HL7.ack;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentState;
import integration.core.domain.configuration.ComponentType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowStepActionType;
import integration.core.dto.MessageFlowStepDto;
import integration.core.messaging.component.adapter.BaseInboundAdapter;

/**
 * Base class for all MLLP/HL7 inbound communication points. This components reads the
 * HL7 message, stores it, writes an event and returns an ACK to the sender.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseMllpInboundAdapter extends BaseInboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMllpInboundAdapter.class);

    private static final String CONTENT_TYPE = "HL7";
    
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
    
    
    public String getHost() {
        return "0.0.0.0";
    }
    
    public String getPort() {
        return componentProperties.get("PORT");
    }

    @Override
    public String getFromUriString() {
        String target = getHost() + ":" + getPort();
        return "netty:tcp://" + target +  constructOptions();
    }

    
    @Override
    protected void setDefaultURIOptions() {
        addURIOption("sync", "true");
        addURIOption("encoders", "#hl7encoder");
        addURIOption("decoders", "#hl7decoder");  
    }

    
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.INBOUND_MLLP_ADAPTER;
    }

    @Override
    public ComponentCategory getCategory() {
        return ComponentCategory.INBOUND_ADAPTER;
    }
    
    
    @Override
    public void configure() throws Exception {
        super.configure();
        
    
        // A route to receive a HL7 message via MLLP, store the message, store an event and generate and send the ACK all
        // within a single transaction.  This is the initial entry point for a HL7 message.
        from(getFromUriString())
            .routeId("mllpInboundMessageHandlerRoute-" + getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            .autoStartup(inboundState == ComponentState.RUNNING)
            .transacted()
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        
                        // Store the message received by this inbound adapter.
                        String inboundMessageContent = exchange.getMessage().getBody(String.class);
                        MessageFlowStepDto inboundMessageFlowStepDto = messagingFlowService.recordMessageFlowStep(inboundMessageContent, BaseMllpInboundAdapter.this, getContentType(), null, MessageFlowStepActionType.MESSAGE_RECEIVED_FROM_OUTSIDE_ENGINE);
                        
                        // Set the message flow step id as as a header so it can be used later.
                        exchange.getMessage().setHeader(MESSAGE_FLOW_STEP_ID, inboundMessageFlowStepDto.getId());
                    }
                })
                .transform(ack())
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        long inboundMessageFlowStepId = (Long)exchange.getMessage().getHeader(MESSAGE_FLOW_STEP_ID);
                        
                        // Store the ACK
                        String ackContent = exchange.getMessage().getBody(String.class);
                        messagingFlowService.recordMessageFlowStep(ackContent, BaseMllpInboundAdapter.this, inboundMessageFlowStepId, "HL7 ACK", null, MessageFlowStepActionType.ACKNOWLEDGMENT_SENT);
                                                
                        // Final step in the inbound message handling is to write an event which will put the message onto a queue for this components outbound message handler to pick up and process.
                        messagingFlowService.recordMessageFlowEvent(inboundMessageFlowStepId,getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE); 
                    }
                });
    }        
}

