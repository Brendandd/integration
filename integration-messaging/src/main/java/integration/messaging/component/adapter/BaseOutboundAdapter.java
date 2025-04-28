package integration.messaging.component.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowStepDto;
import integration.messaging.component.MessageConsumer;
import integration.messaging.component.MessageProducer;
import integration.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * Base class for all outbound adapters.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseOutboundAdapter extends BaseAdapter implements MessageConsumer  {
    protected List<MessageProducer> messageProducers = new ArrayList<>();
    
    public BaseOutboundAdapter(String componentName) {
        super(componentName);
    }

    
    @Override
    public void addMessageProducer(MessageProducer messageProducer) {
        if (!messageProducers.contains(messageProducer)) {
            this.messageProducers.add(messageProducer);
            messageProducer.addMessageConsumer(this);
        }
    }
    
    public abstract String getToUriString();

    
    @Override
    public void configure() throws Exception {
        super.configure();

        // Creates one or more routes based on this components source components.  Each route reads from a topic. This is the entry point for outbound route connectors.
        for (MessageProducer messageProducer : messageProducers) {
            String componentPath = messageProducer.getIdentifier().getComponentPath();
            
            from("jms:VirtualTopic." + componentPath + "::Consumer." + identifier.getComponentPath() + ".VirtualTopic." + componentPath + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
                .routeId("messageReceiver-" + identifier.getComponentPath() + "-" + componentPath)
                .routeGroup(identifier.getComponentPath())
                .autoStartup(isInboundRunning)
                .transacted()
                    .process(new Processor() {
                    
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // Record the outbound message.
                            Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                            MessageFlowStepDto messageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                                                       
                            MessageFlowPolicyResult result = getMessageAcceptancePolicy().applyPolicy(messageFlowStepDto);
                            if (result.isSuccess()) {
                                // Record the content received by this component.
                                long newMessageFlowStepId = messagingFlowService.recordConsumedMessage(BaseOutboundAdapter.this, parentMessageFlowStepId, getContentType());
                            
                                // Record an event so the message can be forwarded to other components for processing.
                                messagingFlowService.recordMessageFlowEvent(newMessageFlowStepId, MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE); 
                            } else {
                                // TODO filter the message
                            }
                        }
                    });
        }

        
        // Entry point for a outbound route connector outbound message handling. 
        from("direct:outboundMessageHandling-" + identifier.getComponentPath())
            .routeId("outboundMessageHandling-" + identifier.getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(identifier.getComponentPath())
            .autoStartup(isInboundRunning)

                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {                       
                        Long parentMessageFlowStepId = (Long)exchange.getMessage().getHeader(MESSAGE_FLOW_STEP_ID);
                        MessageFlowStepDto messageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                        
                        long newMessageFlowStepId = messagingFlowService.recordMessageDispatchedByOutboundHandler(messageFlowStepDto.getMessageContent(), BaseOutboundAdapter.this, parentMessageFlowStepId, getContentType());
                        messagingFlowService.recordMessageFlowEvent(newMessageFlowStepId, MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                    }
                });
    }
}
