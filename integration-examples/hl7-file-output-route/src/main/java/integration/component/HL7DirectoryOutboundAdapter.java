package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.annotation.IntegrationComponent;
import integration.core.messaging.component.type.adapter.directory.annotation.FileNaming;
import integration.messaging.hl7.component.adapter.directory.BaseHL7OutboundDirectoryAdapter;

/**
 * Component to write the file.
 * 
 * @author Brendan Douglas
 * 
 *         TODO filename not retained in all situations.
 * 
 */
@IntegrationComponent(name = "To-Sydney-Hospital-Directory-Outbound-Adapter")
@AllowedContentType(ContentTypeEnum.HL7)
@FileNaming(strategy = "customNamingStrategy")
public class HL7DirectoryOutboundAdapter extends BaseHL7OutboundDirectoryAdapter {
       
}
