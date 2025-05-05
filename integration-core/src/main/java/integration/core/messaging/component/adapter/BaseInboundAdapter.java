package integration.core.messaging.component.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowStepActionType;
import integration.core.dto.MessageFlowStepDto;
import integration.core.exception.EventProcessingException;
import integration.core.messaging.component.BaseMessagingComponent;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * Base class for all inbound adapters.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseInboundAdapter extends BaseAdapter implements MessageProducer {
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();

    @Override
    public void addMessageConsumer(MessageConsumer messageConsumer) {
        if (!messageConsumers.contains(messageConsumer)) {
            this.messageConsumers.add(messageConsumer);
            messageConsumer.addMessageProducer(this);
        }
    }

    
    /**
     * 
     * 
     * @return
     */
    public abstract String getFromUriString();

    
    @Override
    public void configure() throws Exception {
        super.configure();
                
        // Entry point for an inbound adapters outbound message handling. 
        from("direct:outboundMessageHandling-" + getComponentPath())
            .routeId("outboundMessageHandling-" + getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                        MessageFlowStepDto parentMessageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                                               
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowStepDto);
                                  
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            MessageFlowStepDto messageFlowStepDto = messagingFlowService.recordMessageFlowStep(BaseInboundAdapter.this, parentMessageFlowStepId, null, MessageFlowStepActionType.MESSAGE_FORWARDED);
                            messagingFlowService.recordMessageFlowEvent(messageFlowStepDto.getId(), getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                        } else {
                            messagingFlowService.recordMessageNotForwarded(BaseInboundAdapter.this, parentMessageFlowStepDto.getId(), result, MessageFlowStepActionType.MESSAGE_NOT_FORWARDED);
                        }
                    }
                });

        
        // A route to process outbound message handling complete events.  This is the final stage of an inbound adapter.
        from("direct:handleOutboundMessageHandlingCompleteEvent-" + getComponentPath())
            .routeId("handleOutboundMessageHandlingCompleteEvent-" + getComponentPath())
            .routeGroup(getComponentPath())
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
                        
                            // Set the message flow step id as the exchange message body so it can be added to the queue.
                            messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_STEP_ID);
                            
                            producerTemplate.sendBody("jms:topic:VirtualTopic." + getComponentPath(), messageFlowId);
                        } catch(Exception e) {
                            throw new EventProcessingException("Error adding message step flow id to the topic", eventId, messageFlowId, getComponentPath(), e);
                        }
                    }
                });
    }
}

