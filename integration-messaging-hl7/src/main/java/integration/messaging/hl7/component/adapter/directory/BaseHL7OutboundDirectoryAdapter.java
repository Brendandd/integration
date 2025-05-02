package integration.messaging.hl7.component.adapter.directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.messaging.component.adapter.directory.BaseDirectoryOutboundAdapter;

/**
 * 
 */
public abstract class BaseHL7OutboundDirectoryAdapter extends BaseDirectoryOutboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseHL7InboundDirectoryAdapter.class);

    private static final String CONTENT_TYPE = "HL7";

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }
    
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    // TODO complete functionality.
}
