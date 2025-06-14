package integration.core.runtime.messaging.component.type.adapter.smb.inbound;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.runtime.messaging.component.type.adapter.inbound.BaseInboundAdapterOutboxEventProcessor;

/**
 * Outbox event processor for an SMB inbound adapter.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SMBInboundAdapterOutboxEventProcessor extends BaseInboundAdapterOutboxEventProcessor {

}
