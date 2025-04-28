package integration.messaging.component.handler.transformation;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowStepDto;
import integration.messaging.component.BaseMessagingComponent;
import integration.messaging.component.handler.MessageHandler;
import integration.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * Base class for all transformation processing steps.
 */
public abstract class BaseTransformationProcessingStep extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTransformationProcessingStep.class);

    public BaseTransformationProcessingStep(String componentName) {
        super(componentName);
    }
    
    @Override
    public Logger getLogger() {
        return LOGGER;
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
        from("direct:outboundMessageHandling-" + identifier.getComponentPath())
            .routeId("outboundMessageHandling-" + identifier.getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(identifier.getComponentPath())
            .autoStartup(isInboundRunning)
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Record the outbound message.
                        Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                        MessageFlowStepDto messageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                        
                        String transformedContent = getTransformer().transform(messageFlowStepDto);
                        
                        // Need to update the message content before applying the policy.
                        messageFlowStepDto.getMessage().setContent(transformedContent);
                                                     
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(messageFlowStepDto);
                                                                       
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            long newMessageFlowStepId = messagingFlowService.recordMessageDispatchedByOutboundHandler(transformedContent, BaseTransformationProcessingStep.this,parentMessageFlowStepId, getContentType());
                            messagingFlowService.recordMessageFlowEvent(newMessageFlowStepId, MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                        } else {
                            // filter message.
                        }
                    }
                });

        
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
                        exchange.getMessage().setBody(messageFlowId);
                    }
                })
            .to("jms:topic:VirtualTopic." + identifier.getComponentPath());
    }
}