package integration.core.runtime.messaging.component.type.handler.splitter;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.runtime.messaging.component.BaseComponentConnectorOutboxEventProcessor;


/**
 * Outbox event processor for a all message splitters.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SplitterOutboxEventProcessor extends BaseComponentConnectorOutboxEventProcessor<BaseSplitterComponent> {
    
}
