package integration.messaging.hl7.component.adapter.mllp;

import static org.apache.camel.component.hl7.HL7.ack;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.messaging.component.adapter.BaseInboundAdapter;

/**
 * Base class for all MLLP/HL7 inbound communication points. This components reads the
 * HL7 message, stores it, writes an event and returns an ACK to the sender.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseMllpInboundAdapter extends BaseInboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMllpInboundAdapter.class);

    public BaseMllpInboundAdapter(String componentName) throws Exception {
        super(componentName);
    }

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
    public void configure() throws Exception {
        super.configure();
        
    
        // A route to receive a HL7 message via MLLP, store the message, store an event and generate and send the ACK all
        // within a single transaction.  This is the initial entry point for a HL7 message.
        from(getFromUriString())
            .routeId("mllpInboundMessageHandlerRoute-" + identifier.getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(identifier.getComponentPath())
            .autoStartup(isInboundRunning)
            .transacted()
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Store the message received by this inbound adapter.
                        String messageContent = exchange.getMessage().getBody(String.class);
                        long messageFlowStepId = messagingFlowService.recordMessageReceivedFromExternalSource(messageContent, BaseMllpInboundAdapter.this, CONTENT_TYPE);
                        
                        // Set the message flow step id as as a header so it can be used later.
                        exchange.getMessage().setHeader(MESSAGE_FLOW_STEP_ID, messageFlowStepId);
                    }
                })
                .transform(ack())
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        long messageFlowStepId = (Long)exchange.getMessage().getHeader(MESSAGE_FLOW_STEP_ID);
                        
                        // Store the ACK
                        String ackContent = exchange.getMessage().getBody(String.class);
                        messagingFlowService.recordAck(ackContent, BaseMllpInboundAdapter.this, messageFlowStepId, "HL7 ACK");
                        
                        // Final step in the inbound message handling is to write an event which will put the message onto a queue for this components outbound message handler to pick up and process.
                        messagingFlowService.recordMessageFlowEvent(messageFlowStepId, MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE); 
                    }
                });
    }        
}

