package integration.core.runtime.messaging.component.type.handler.splitter;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.handler.MessageHandler;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.splitter.annotation.UsesSplitter;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;

/**
 * Base class for splitting a message into 1 or more messages. A splitter is
 * only responsible for splitting a message and no transformations should be
 * done as part of the splitting process.
 */
@ComponentType(type = IntegrationComponentTypeEnum.SPLITTER)
public abstract class BaseSplitterProcessingStep extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSplitterProcessingStep.class);

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    public MessageSplitter getSplitter() throws ComponentConfigurationException {
        UsesSplitter annotation = getRequiredAnnotation(UsesSplitter.class);
        
        return springContext.getBean(annotation.name(), MessageSplitter.class);  
    }
    
    
    @Override
    public void configure() throws Exception {
        super.configure();
   
        
        // Entry point for an inbound adapters outbound message handling. 
        from("direct:outboundMessageHandling-" + getIdentifier())
            .routeId("outboundMessageHandling-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        
                        // Record the outbound message.
                        long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                        exchange.getMessage().setHeader(MESSAGE_FLOW_ID, parentMessageFlowId);
                        
                        MessageFlowDto parentMessageFlowDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowId);
                        
                        String[] splitMessages = getSplitter().split(parentMessageFlowDto);
                                  
                        for (int i = 0; i < splitMessages.length; i++) {
                            MessageFlowDto splitMessageFlowDto = messagingFlowService.recordMessageFlow(getIdentifier(),parentMessageFlowId, MessageFlowActionType.CREATED_FROM_SPLIT);
                                               
                            MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(splitMessageFlowDto);
                                                                              
                            // Apply the message forwarding rules and either write an event for further processing or filter the message.
                            if (result.isSuccess()) {           
                                MessageFlowDto forwardedMessageFlowDto = messagingFlowService.recordMessageFlow(getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.PENDING_FORWARDING);
                                messageFlowEventService.recordMessageFlowEvent(forwardedMessageFlowDto.getId(),getIdentifier(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                            } else {
                                messagingFlowService.recordMessageNotForwarded(getIdentifier(), splitMessageFlowDto.getId(), result, MessageFlowActionType.NOT_FORWARDED);
                            }
                        }
                       
                    }
                });
        }

    
    @Override
    protected void configureRequiredAnnotations() {    
        super.configureRequiredAnnotations();
        
        requiredAnnotations.add(UsesSplitter.class);
    }
}
