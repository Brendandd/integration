package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.adapter.AdapterOption;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;
import integration.messaging.hl7.component.adapter.directory.BaseHL7InboundDirectoryAdapter;

/**
 * Reads a file from the configured folder.
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "directory-inbound")
@AdapterOption(key = "idempotent", value = "true")
@AdapterOption(key = "idempotentRepository", value = "#jpaStore")
@AdapterOption(key = "move", value = "processed")
@AdapterOption(key = "noop", value = "false")
@ForwardingPolicy(name = "forwardAllMessages")
@AllowedContentType(ContentTypeEnum.HL7)
public class HL7DirectoryInboundAdapter extends BaseHL7InboundDirectoryAdapter {
 
}
