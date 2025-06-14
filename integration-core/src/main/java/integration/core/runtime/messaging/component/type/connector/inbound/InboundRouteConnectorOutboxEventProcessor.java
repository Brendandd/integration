package integration.core.runtime.messaging.component.type.connector.inbound;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.runtime.messaging.component.BaseComponentConnectorOutboxEventProcessor;


/**
 * Outbox event processor for inbound route connectors.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InboundRouteConnectorOutboxEventProcessor extends BaseComponentConnectorOutboxEventProcessor<BaseInboundRouteConnectorComponent> {
    
   
}
