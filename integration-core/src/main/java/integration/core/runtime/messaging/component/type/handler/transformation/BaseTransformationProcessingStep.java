package integration.core.runtime.messaging.component.type.handler.transformation;

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
import integration.core.runtime.messaging.component.type.handler.transformation.annotation.UsesTransformer;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;

/**
 * Base class for all transformation processing steps.
 */
@ComponentType(type = IntegrationComponentTypeEnum.TRANSFORMER)
public abstract class BaseTransformationProcessingStep extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTransformationProcessingStep.class);

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
    public MessageTransformer getTransformer() throws ComponentConfigurationException {
        UsesTransformer annotation = getRequiredAnnotation(UsesTransformer.class);
        
        return springContext.getBean(annotation.name(), MessageTransformer.class);  
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
                        
                        // Transform the content.
                        String transformedContent = getTransformer().transform(parentMessageFlowDto);
                        
                        MessageFlowDto transformedMessageFlowDto = messagingFlowService.recordMessageFlow(transformedContent, getIdentifier(),parentMessageFlowId, getContentType(), MessageFlowActionType.TRANSFORMED);
                        
                        // Need to update the message content before applying the policy.
                        parentMessageFlowDto.getMessage().setContent(transformedContent);
                                                     
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowDto);
                                                                       
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            MessageFlowDto forwardedMessageFlowDto = messagingFlowService.recordMessageFlow(getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.PENDING_FORWARDING);
                            messageFlowEventService.recordMessageFlowEvent(forwardedMessageFlowDto.getId(),getIdentifier(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                        } else {
                            messagingFlowService.recordMessageNotForwarded(getIdentifier(), transformedMessageFlowDto.getId(), result, MessageFlowActionType.NOT_FORWARDED);
                        }
                    }
                });
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        super.configureRequiredAnnotations();
        requiredAnnotations.add(UsesTransformer.class);
    }
}