package integration.core.messaging.component.handler.splitter;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowStepActionType;
import integration.core.dto.MessageFlowStepDto;
import integration.core.messaging.component.handler.MessageHandler;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * Base class for splitting a message into 1 or more messages. A splitter is
 * only responsible for splitting a message and no transformations should be
 * done as part of the splitting process.
 */
public abstract class BaseSplitterProcessingStep extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSplitterProcessingStep.class);

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    public abstract MessageSplitter getSplitter();
    
    @Override
    public ComponentType getType() {
        return ComponentType.SPLITTER;
    }

    @Override
    public ComponentCategory getCategory() {
        return ComponentCategory.MESSAGE_HANDLER;
    }

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
                        // Record the outbound message.
                        Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                        MessageFlowStepDto parentMessageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                        
                        String[] splitMessages = getSplitter().split(parentMessageFlowStepDto);
                                  
                        for (int i = 0; i < splitMessages.length; i++) {
                            MessageFlowStepDto splitMessageFlowStepDto = messagingFlowService.recordMessageFlowStep(BaseSplitterProcessingStep.this,parentMessageFlowStepId, null, MessageFlowStepActionType.CREATED_FROM_SPLIT);
                                               
                            MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(splitMessageFlowStepDto);
                                                                              
                            // Apply the message forwarding rules and either write an event for further processing or filter the message.
                            if (result.isSuccess()) {           
                                messagingFlowService.recordMessageFlowEvent(splitMessageFlowStepDto.getId(),getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                            } else {
                                messagingFlowService.recordMessageNotForwarded(BaseSplitterProcessingStep.this, splitMessageFlowStepDto.getId(), result, MessageFlowStepActionType.NOT_FORWARDED);
                            }
                        }
                    }
                });
        }
}
