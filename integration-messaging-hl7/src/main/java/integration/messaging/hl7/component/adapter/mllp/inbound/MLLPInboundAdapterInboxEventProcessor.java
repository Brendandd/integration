package integration.messaging.hl7.component.adapter.mllp.inbound;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.runtime.messaging.component.type.adapter.inbound.BaseInboundAdapterInboxEventProcessor;

/**
 * Inbox event processor for an MLLP inbound adapter.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MLLPInboundAdapterInboxEventProcessor extends BaseInboundAdapterInboxEventProcessor {
    
}
