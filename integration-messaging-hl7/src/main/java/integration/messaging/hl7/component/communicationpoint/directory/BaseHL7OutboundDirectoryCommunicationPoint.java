package integration.messaging.hl7.component.communicationpoint.directory;

import integration.messaging.component.communicationpoint.directory.BaseDirectoryOutboundCommunicationPoint;

/**
 * 
 */
public abstract class BaseHL7OutboundDirectoryCommunicationPoint extends BaseDirectoryOutboundCommunicationPoint {

    public BaseHL7OutboundDirectoryCommunicationPoint(String componentName) {
        super(componentName);
    }

    private static final String CONTENT_TYPE = "HL7";

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    // TODO complete functionality.
}
