package integration.core.runtime.messaging.component.type.handler.filter;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.handler.BaseMessageHandlerComponent;

/**
 * Base class for all filter processing steps.
 */
@ComponentType(type = IntegrationComponentTypeEnum.FILTER)
public abstract class BaseFilterComponent extends BaseMessageHandlerComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFilterComponent.class);

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @Override
    protected void configureComponentLevelExceptionHandlers() {
        
        // Handle filter errors
        onException(FilterException.class)
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
}
