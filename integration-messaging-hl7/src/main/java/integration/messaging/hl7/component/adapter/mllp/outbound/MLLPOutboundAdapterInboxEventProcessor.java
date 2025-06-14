package integration.messaging.hl7.component.adapter.mllp.outbound;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.runtime.messaging.component.type.adapter.outbound.BaseOutboundAdapterInboxEventProcessor;


/**
 * Inbox event processor for an SMB outbound adapter.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MLLPOutboundAdapterInboxEventProcessor extends BaseOutboundAdapterInboxEventProcessor {
}
