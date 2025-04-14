package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import integration.messaging.component.processingstep.filter.MessageAcceptancePolicy;
import integration.messaging.hl7.component.communicationpoint.directory.BaseHL7OutboundDirectoryCommunicationPoint;

/**
 * Component to write the file.
 * 
 * @author Brendan Douglas
 * 
 *         TODO filename not retained in all situations.
 * 
 */
@Component
public class HL7DirectoryOutboundCommunicationPoint extends BaseHL7OutboundDirectoryCommunicationPoint {

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;

    public HL7DirectoryOutboundCommunicationPoint() {
        super("directory-outbound");
    }

    private static final String CONTENT_TYPE = "HL7";

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() {
        return messageAcceptancePolicy;
    }
}
