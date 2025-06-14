package integration.core.runtime.messaging.component.type.handler.filter;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.handler.BaseMessageHandlerComponent;
import jakarta.annotation.PostConstruct;

/**
 * Base class for all filter processing steps.
 */
@ComponentType(type = IntegrationComponentTypeEnum.FILTER)
public abstract class BaseFilterComponent extends BaseMessageHandlerComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFilterComponent.class);
    
    @Autowired
    private FilterInboxEventProcessor inboxEventProcessor;
    
    @Autowired
    private FilterOutboxEventProcessor outboxEventProcessor;

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
    
    
    @PostConstruct
    public void BaseFilterComponentInit() {
        inboxEventProcessor.setComponent(this);
        outboxEventProcessor.setComponent(this);
    }

    
    @Override
    protected void configureComponentLevelExceptionHandlers() {
        
        // Handle filter errors
        onException(FilterException.class)
        .maximumRedeliveries(0)
        .process(exchange -> {           
            FilterException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, FilterException.class);
            getLogger().error("Full exception trace", theException);
            getLogger().warn("Filter exception - summary: {}", theException); 
            
            Long messageFlowId = getMessageFlowId(theException, exchange);
            
            if (!theException.isRetryable() && messageFlowId != null) {
                messageFlowService.recordFilterError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true);         
    }
    
    
    @Override
    public FilterInboxEventProcessor getInboxEventProcessor() {
        return inboxEventProcessor;
    }

    
    @Override
    public FilterOutboxEventProcessor getOutboxEventProcessor() {
        return outboxEventProcessor;
    }
}
