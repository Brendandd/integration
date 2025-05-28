package integration.core.runtime.messaging.component.type.handler.transformation;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.handler.ProcessingMessageHandlerComponent;
import integration.core.runtime.messaging.component.type.handler.transformation.annotation.UsesTransformer;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import jakarta.annotation.PostConstruct;

/**
 * Base class for all transformation processing steps.
 */
@ComponentType(type = IntegrationComponentTypeEnum.TRANSFORMER)
public abstract class BaseTransformationComponent extends ProcessingMessageHandlerComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTransformationComponent.class);

    @Autowired
    private MessageTransformationProcessor messageTransformationProcessor;
    
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
    
    @PostConstruct
    public void init() {
        messageTransformationProcessor.setComponent(this);
        egressQueueConsumerWithForwardingPolicyProcessor.setComponent(this);
        ingressTopicConsumerWithAcceptancePolicyProcessor.setComponent(this);
    }

    
    @Override
    protected void configureComponentLevelExceptionHandlers() {
        // Handle transformation errors.
        onException(TransformationException.class)
        .process(exchange -> {           
            TransformationException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, TransformationException.class);
            getLogger().error("Full exception trace", theException);
            getLogger().warn("Transformation exception - summary: {}", theException); 
            
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
    protected void configureProcessingQueueConsumer() throws ComponentConfigurationException, RouteConfigurationException {
        from("jms:queue:processingQueue-" + getIdentifier() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
        .routeId("startProcessing-" + getIdentifier())
        .routeGroup(getComponentPath())
        .setHeader("contentType", constant(getContentType()))
        .transacted()   
            .process(messageTransformationProcessor);
        
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        super.configureRequiredAnnotations();
        requiredAnnotations.add(UsesTransformer.class);
    }
}