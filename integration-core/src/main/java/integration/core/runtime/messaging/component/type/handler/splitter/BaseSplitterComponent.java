package integration.core.runtime.messaging.component.type.handler.splitter;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.handler.BaseMessageHandlerComponent;
import integration.core.runtime.messaging.component.type.handler.splitter.annotation.UsesSplitter;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import jakarta.annotation.PostConstruct;

/**
 * Base class for splitting a message into 1 or more messages. A splitter is
 * only responsible for splitting a message and no transformations should be
 * done as part of the splitting process.
 */
@ComponentType(type = IntegrationComponentTypeEnum.SPLITTER)
public abstract class BaseSplitterComponent extends BaseMessageHandlerComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSplitterComponent.class);
    
    @Autowired
    private SplitterInboxEventProcessor inboxEventProcessor;
    
    @Autowired
    private SplitterOutboxEventProcessor outboxEventProcessor;

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @PostConstruct
    public void BaseSplitterComponentInit() {
        inboxEventProcessor.setComponent(this);
        outboxEventProcessor.setComponent(this);
    }

    
    public MessageSplitter getSplitter() throws ComponentConfigurationException {
        UsesSplitter annotation = getRequiredAnnotation(UsesSplitter.class);
        
        return springContext.getBean(annotation.name(), MessageSplitter.class);  
    }

    
    @Override
    protected void configureComponentLevelExceptionHandlers() {
        // Handle splitter errors
        onException(SplitterException.class)
        .maximumRedeliveries(0)
        .process(exchange -> {            
            SplitterException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, SplitterException.class);
            getLogger().error("Full exception trace", theException);
            getLogger().warn("Splitter exception - summary: {}", theException); 
            
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
    protected void configureRequiredAnnotations() {    
        super.configureRequiredAnnotations();
        
        requiredAnnotations.add(UsesSplitter.class);
    }

    
    @Override
    public SplitterInboxEventProcessor getInboxEventProcessor() {
        return inboxEventProcessor;
    }

    
    @Override
    public SplitterOutboxEventProcessor getOutboxEventProcessor() {
        return outboxEventProcessor;
    }  
}
