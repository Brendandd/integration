package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.annotation.IntegrationComponent;
import integration.core.runtime.messaging.component.type.adapter.annotation.AdapterOption;
import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNaming;
import integration.messaging.hl7.component.adapter.smb.BaseHL7OutboundSMBAdapter;

/**
 * Component to write the file.
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "SMB-Outbound-Adapter")
@AdapterOption(key = "username", value = "${SMB_USER_NAME}") 
@AdapterOption(key = "password", value = "${SMB_PASSWORD}") 
@AllowedContentType(ContentTypeEnum.HL7)
@FileNaming(strategy = "customNamingStrategy")
//@LoadHeader(name = "camelFileName")
public class SMBOutboundAdapter extends BaseHL7OutboundSMBAdapter {
       
}
