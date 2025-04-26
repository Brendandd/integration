package integration.messaging.hl7.component.adapter.directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.messaging.component.adapter.directory.BaseDirectoryInboundAdapter;

/**
 * 
 */
public abstract class BaseHL7InboundDirectoryAdapter extends BaseDirectoryInboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseHL7InboundDirectoryAdapter.class);
    
    public BaseHL7InboundDirectoryAdapter(String componentName) {
        super(componentName);
    }

    private static final String CONTENT_TYPE = "HL7";

    
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    // TODO complete functionality.
}
