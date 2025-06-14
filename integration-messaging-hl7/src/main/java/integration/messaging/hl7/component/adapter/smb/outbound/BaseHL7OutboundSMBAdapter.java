package integration.messaging.hl7.component.adapter.smb.outbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.type.adapter.smb.outbound.BaseSMBOutboundAdapter;

/**
 * 
 */
@AllowedContentType(ContentTypeEnum.HL7)
public abstract class BaseHL7OutboundSMBAdapter extends BaseSMBOutboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseHL7OutboundSMBAdapter.class);
   
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    // TODO complete functionality.
}
