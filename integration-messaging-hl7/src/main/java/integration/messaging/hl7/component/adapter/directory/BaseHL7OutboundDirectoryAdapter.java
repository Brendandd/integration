package integration.messaging.hl7.component.adapter.directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.type.adapter.directory.BaseDirectoryOutboundAdapter;

/**
 * 
 */
@AllowedContentType(ContentTypeEnum.HL7)
public abstract class BaseHL7OutboundDirectoryAdapter extends BaseDirectoryOutboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseHL7InboundDirectoryAdapter.class);
   
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    // TODO complete functionality.
}
