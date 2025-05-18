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
@AdapterOption(key = "username", value = "testOut") //testing only. 
@AdapterOption(key = "password", value = "testpass") //testing only
@AllowedContentType(ContentTypeEnum.HL7)
@FileNaming(strategy = "customNamingStrategy")
public class SMBOutboundAdapter extends BaseHL7OutboundSMBAdapter {
       
}
