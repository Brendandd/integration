package integration.messaging.component.handler.splitter;

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
 * Base class for splitting a message into 1 or more messages. A splitter is
 * only responsible for splitting a message and no transformations should be
 * done as part of the splitting process.
 */
public abstract class BaseSplitterProcessingStep extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSplitterProcessingStep.class);

    public BaseSplitterProcessingStep(String componentName) {
        super(componentName);
    }
    
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    public abstract MessageSplitter getSplitter();
    
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
                        MessageFlowStepDto parentMessageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                        
                        String[] splitMessages = getSplitter().split(parentMessageFlowStepDto);
                                  
                        for (int i = 0; i < splitMessages.length; i++) {
                                               
                            MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowStepDto);
                                                                              
                            // Apply the message forwarding rules and either write an event for further processing or filter the message.
                            if (result.isSuccess()) {
                                MessageFlowStepDto messageFlowStepDto = messagingFlowService.recordOutboundMessageHandlerComplete(parentMessageFlowStepDto.getMessageContent(), BaseSplitterProcessingStep.this,parentMessageFlowStepId, getContentType());
                                
                                messagingFlowService.recordMessageFlowEvent(messageFlowStepDto.getId(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                            } else {
                                // filter message.
                            }
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
