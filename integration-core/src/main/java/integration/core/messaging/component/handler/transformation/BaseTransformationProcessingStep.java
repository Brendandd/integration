package integration.core.messaging.component.handler.transformation;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowStepActionType;
import integration.core.dto.MessageFlowStepDto;
import integration.core.messaging.component.BaseMessagingComponent;
import integration.core.messaging.component.handler.MessageHandler;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * Base class for all transformation processing steps.
 */
public abstract class BaseTransformationProcessingStep extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTransformationProcessingStep.class);

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.TRANSFORMER;
    }

    @Override
    public ComponentCategory getCategory() {
        return ComponentCategory.MESSAGE_HANDLER;
    }

    
    /**
     * The transformer. A transformer is responsible for transforming the message. A
     * transformer can also filter a message is required.
     * 
     * @return
     */
    public abstract MessageTransformer getTransformer();
    
    @Override
    public void configure() throws Exception {
        super.configure();
        

        // Entry point for an inbound adapters outbound message handling. 
        from("direct:outboundMessageHandling-" + getComponentPath())
            .routeId("outboundMessageHandling-" + getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            .autoStartup(isInboundRunning)
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Record the outbound message.
                        Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                        MessageFlowStepDto parentMessageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                        
                        String transformedContent = getTransformer().transform(parentMessageFlowStepDto);
                        
                        // Need to update the message content before applying the policy.
                        parentMessageFlowStepDto.getMessage().setContent(transformedContent);
                                                     
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowStepDto);
                                                                       
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            MessageFlowStepDto messageFlowStepDto = messagingFlowService.recordMessageFlowStep(transformedContent, BaseTransformationProcessingStep.this,parentMessageFlowStepId, getContentType(), null, MessageFlowStepActionType.MESSAGE_FORWARDED);
                            messagingFlowService.recordMessageFlowEvent(messageFlowStepDto.getId(),getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                        } else {
                            messagingFlowService.recordMessageNotForwarded(BaseTransformationProcessingStep.this, parentMessageFlowStepDto.getId(), result, MessageFlowStepActionType.MESSAGE_NOT_FORWARDED);
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
                        // Delete the event.
                        Long eventId = exchange.getMessage().getBody(Long.class);
                        messagingFlowService.deleteEvent(eventId);
                        
                        // Set the message flow step id as the exchange message body so it can be added to the queue.
                        Long messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_STEP_ID);
                        exchange.getMessage().setBody(messageFlowId);
                    }
                })
            .to("jms:topic:VirtualTopic." + getComponentPath());
    }
}