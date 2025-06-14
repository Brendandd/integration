package integration.messaging.hl7.component.adapter.smb.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.type.adapter.smb.inbound.BaseSMBInboundAdapter;

/**
 * 
 */
@AllowedContentType(ContentTypeEnum.HL7)
public abstract class BaseHL7InboundSMBAdapter extends BaseSMBInboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseHL7InboundSMBAdapter.class);
       
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
