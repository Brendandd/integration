package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import integration.messaging.component.processingstep.filter.MessageForwardingPolicy;
import integration.messaging.hl7.component.communicationpoint.directory.BaseHL7InboundDirectoryCommunicationPoint;

/**
 * Reads a file from the configured folder.
 * 
 * @author Brendan Douglas
 * 
 */
@Component
public class HL7DirectoryInboundCommunicationPoint extends BaseHL7InboundDirectoryCommunicationPoint {

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    public HL7DirectoryInboundCommunicationPoint() {
        super("directory-inbound");
    }

    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        return messageForwardingPolicy;
    }
}
