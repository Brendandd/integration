package integration.core.messaging.component.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import integration.core.domain.configuration.ComponentState;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowStepActionType;
import integration.core.dto.MessageFlowStepDto;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * Base class for all outbound adapters.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseOutboundAdapter extends BaseAdapter implements MessageConsumer  {
    protected List<MessageProducer> messageProducers = new ArrayList<>();
    
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
            
            from("jms:VirtualTopic." + messageProducer.getComponentPath() + "::Consumer." + getComponentPath() + ".VirtualTopic." + messageProducer.getComponentPath() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
                .routeId("messageReceiver-" + messageProducer.getComponentPath() + "-" + messageProducer.getComponentPath())
                .routeGroup(getComponentPath())
                .autoStartup(inboundState == ComponentState.RUNNING)
                .transacted()
                    .process(new Processor() {
                    
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // Record the outbound message.
                            Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                            MessageFlowStepDto parentMessageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                                                       
                            MessageFlowPolicyResult result = getMessageAcceptancePolicy().applyPolicy(parentMessageFlowStepDto);
                            if (result.isSuccess()) {
                                // Record the content received by this component.
                                MessageFlowStepDto messageFlowStepDto = messagingFlowService.recordMessageFlowStep(BaseOutboundAdapter.this, parentMessageFlowStepId, null, MessageFlowStepActionType.MESSAGE_ACCEPTED);
                            
                                // Record an event so the message can be forwarded to other components for processing.
                                messagingFlowService.recordMessageFlowEvent(messageFlowStepDto.getId(),getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE); 
                            } else {
                                messagingFlowService.recordMessageNotAccepted(BaseOutboundAdapter.this, parentMessageFlowStepId, result, MessageFlowStepActionType.MESSAGE_NOT_ACCEPTED);
                            }
                        }
                    });
        }

        
        // Entry point for a outbound route connectors outbound message handling. 
        from("direct:outboundMessageHandling-" + getComponentPath())
            .routeId("outboundMessageHandling-" + getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())

                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {                       
                        Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                        
                        MessageFlowStepDto messageFlowStepDto = messagingFlowService.recordMessageFlowStep(BaseOutboundAdapter.this, parentMessageFlowStepId, null, MessageFlowStepActionType.MESSAGE_DISPATCHED_TO_OUTSIDE_ENGINE);
                        messagingFlowService.recordMessageFlowEvent(messageFlowStepDto.getId(), getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                    }
                });
    }
}
