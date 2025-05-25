package integration.core.runtime.messaging.component.type.handler.transformation;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.handler.ProcessingMessageHandlerComponent;
import integration.core.runtime.messaging.component.type.handler.transformation.annotation.UsesTransformer;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;

/**
 * Base class for all transformation processing steps.
 */
@ComponentType(type = IntegrationComponentTypeEnum.TRANSFORMER)
public abstract class BaseTransformationComponent extends ProcessingMessageHandlerComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTransformationComponent.class);

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @Override
    public void configureComponentLevelExceptionHandlers() {
        // Handle transformation errors.
        onException(TransformationException.class)
        .process(exchange -> {           
            TransformationException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, TransformationException.class);
            getLogger().error("Transformation exception - " + theException.toString());
            
            Long messageFlowId = getMessageFlowId(theException, exchange);
                    
            if (!theException.isRetryable() && messageFlowId != null) {
                messageFlowService.recordTransformationError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true);         
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
    public void configureProcessingQueueConsumer() throws ComponentConfigurationException, RouteConfigurationException {
        from("jms:queue:processingQueue-" + getIdentifier() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
        .routeId("startProcessing-" + getIdentifier())
        .routeGroup(getComponentPath())
        .setHeader("contentType", constant(getContentType()))
        .transacted()   
            .process(exchange -> {                                      
                MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange);
                
                // Transform the content.
                String transformedContent = getTransformer().transform(parentMessageFlowDto);
                
                MessageFlowDto transformedMessageFlowDto = messageFlowService.recordNewContentMessageFlow(transformedContent, getIdentifier(),parentMessageFlowDto.getId(), getContentType(), MessageFlowActionType.TRANSFORMED);
                outboxService.recordEvent(transformedMessageFlowDto.getId(),getIdentifier(), OutboxEventType.PROCESSING_COMPLETE); 
        });
        
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        super.configureRequiredAnnotations();
        requiredAnnotations.add(UsesTransformer.class);
    }
}