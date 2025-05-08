package integration.messaging.hl7.component.adapter.directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.adapter.directory.BaseDirectoryInboundAdapter;

/**
 * 
 */
@AllowedContentType(ContentTypeEnum.HL7)
public abstract class BaseHL7InboundDirectoryAdapter extends BaseDirectoryInboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseHL7InboundDirectoryAdapter.class);
       
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
