package integration.messaging.hl7.component.adapter.directory;

import integration.messaging.component.adapter.directory.BaseDirectoryInboundAdapter;

/**
 * 
 */
public abstract class BaseHL7InboundDirectoryAdapter extends BaseDirectoryInboundAdapter {
    public BaseHL7InboundDirectoryAdapter(String componentName) {
        super(componentName);
    }

    private static final String CONTENT_TYPE = "HL7";

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    // TODO complete functionality.
}
