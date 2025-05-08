package integration.core.messaging.component.handler.transformation;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentType;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.handler.MessageHandler;
import integration.core.messaging.component.handler.filter.AcceptancePolicy;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;
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
    public MessageTransformer getTransformer() {
        UsesTransformer annotation = this.getClass().getAnnotation(UsesTransformer.class);
        
        if (annotation == null) {
            throw new ConfigurationException("@UsesTransformer annotation not found.  It is mandatory for all transformers");
        }
        
        return springContext.getBean(annotation.name(), MessageTransformer.class);  
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
                        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                        MessageFlowDto parentMessageFlowDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowId);
                        
                        // Transform the content.
                        String transformedContent = getTransformer().transform(parentMessageFlowDto);
                        
                        MessageFlowDto transformedMessageFlowDto = messagingFlowService.recordMessageFlow(transformedContent, BaseTransformationProcessingStep.this,parentMessageFlowId, getContentType(), MessageFlowActionType.TRANSFORMED);
                        
                        // Need to update the message content before applying the policy.
                        parentMessageFlowDto.getMessage().setContent(transformedContent);
                                                     
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowDto);
                                                                       
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            messagingFlowService.recordMessageFlowEvent(transformedMessageFlowDto.getId(),getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                        } else {
                            messagingFlowService.recordMessageNotForwarded(BaseTransformationProcessingStep.this, transformedMessageFlowDto.getId(), result, MessageFlowActionType.NOT_FORWARDED);
                        }
                    }
                });
    }
    
    
    @Override
    protected Set<Class<? extends Annotation>> getAllowedAnnotations() {
        Set<Class<? extends Annotation>> allowedAnnotations = new LinkedHashSet<>();
        
        allowedAnnotations.add(IntegrationComponent.class);
        allowedAnnotations.add(AcceptancePolicy.class);
        allowedAnnotations.add(ForwardingPolicy.class);
        allowedAnnotations.add(AllowedContentType.class);
        allowedAnnotations.add(UsesTransformer.class);

        return allowedAnnotations;
    }
}