package integration.core.runtime.messaging.component.type.handler.splitter;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.handler.ProcessingMessageHandlerComponent;
import integration.core.runtime.messaging.component.type.handler.splitter.annotation.UsesSplitter;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import jakarta.annotation.PostConstruct;

/**
 * Base class for splitting a message into 1 or more messages. A splitter is
 * only responsible for splitting a message and no transformations should be
 * done as part of the splitting process.
 */
@ComponentType(type = IntegrationComponentTypeEnum.SPLITTER)
public abstract class BaseSplitterComponent extends ProcessingMessageHandlerComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSplitterComponent.class);
    
    @Autowired
    private MessageSplitterProcessor messageSplitterProcessor;

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @PostConstruct
    public void init() {
        messageSplitterProcessor.setComponent(this);
    }

    
    public MessageSplitter getSplitter() throws ComponentConfigurationException {
        UsesSplitter annotation = getRequiredAnnotation(UsesSplitter.class);
        
        return springContext.getBean(annotation.name(), MessageSplitter.class);  
    }

    
    @Override
    protected void configureComponentLevelExceptionHandlers() {
        // Handle splitter errors
        onException(SplitterException.class)
        .process(exchange -> {            
            SplitterException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, SplitterException.class);
            getLogger().error("Splitter exception - " + theException.toString());
            
            Long messageFlowId = getMessageFlowId(theException, exchange);
            
            if (!theException.isRetryable() && messageFlowId != null) {
                messageFlowService.recordSplitterError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true);         
    }

    
    @Override
    protected void configureProcessingQueueConsumer() throws ComponentConfigurationException, RouteConfigurationException {

        from("jms:queue:processingQueue-" + getIdentifier() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
            .routeId("startProcessing-" + getIdentifier())
            .routeGroup(getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .transacted()   
            .process(messageSplitterProcessor);
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        super.configureRequiredAnnotations();
        
        requiredAnnotations.add(UsesSplitter.class);
    }
}
