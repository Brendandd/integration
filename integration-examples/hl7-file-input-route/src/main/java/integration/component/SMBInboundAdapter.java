package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.annotation.IntegrationComponent;
import integration.core.runtime.messaging.component.type.adapter.annotation.AdapterOption;
import integration.messaging.hl7.component.adapter.smb.inbound.BaseHL7InboundSMBAdapter;

/**
 * Reads a file from the configured folder,
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "SMB-Inbound-Adapter")
@AdapterOption(key = "username", value = "${SMB_INBOUND_ADAPTER_SAMBA_USER}") 
@AdapterOption(key = "password", value = "${SMB_INBOUND_ADAPTER_SAMBA_PASSWORD}") 
@AdapterOption(key = "idempotent", value = "true")
@AdapterOption(key = "idempotentRepository", value = "#jpaStore")
@AdapterOption(key = "move", value = "processed")
@AdapterOption(key = "noop", value = "false")
@AllowedContentType(ContentTypeEnum.HL7)
public class SMBInboundAdapter extends BaseHL7InboundSMBAdapter {
 
}
