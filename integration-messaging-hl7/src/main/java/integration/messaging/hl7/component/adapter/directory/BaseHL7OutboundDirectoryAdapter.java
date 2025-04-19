package integration.messaging.hl7.component.adapter.directory;

import integration.messaging.component.adapter.directory.BaseDirectoryOutboundAdapter;

/**
 * 
 */
public abstract class BaseHL7OutboundDirectoryAdapter extends BaseDirectoryOutboundAdapter {

    public BaseHL7OutboundDirectoryAdapter(String componentName) {
        super(componentName);
    }

    private static final String CONTENT_TYPE = "HL7";

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    // TODO complete functionality.
}
