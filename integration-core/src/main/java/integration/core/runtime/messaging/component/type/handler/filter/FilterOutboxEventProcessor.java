package integration.core.runtime.messaging.component.type.handler.filter;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.runtime.messaging.component.BaseComponentConnectorOutboxEventProcessor;


/**
 * Outbox event processor for a all message filters.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FilterOutboxEventProcessor extends BaseComponentConnectorOutboxEventProcessor<BaseFilterComponent> {
    
}
