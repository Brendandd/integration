package integration.messaging.hl7.component.processingstep.filter;

import integration.messaging.component.processingstep.filter.FilterException;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * Base class for hl7 message type filters. This type of filter will
 * accept/reject the message based on the type of the hl7 message.
 * 
 * @author Brendan Douglas
 */
public abstract class MessageTypeFilter extends BaseHL7MessageAcceptancePolicy {
    public abstract String[] getAllowedMessageTypes();

    protected String messageType = null;

    @Override
    public boolean applyPolicy(HL7Message source) throws FilterException {

        try {
            String incomingMessageType = source.getMessageTypeField().value();
            messageType = incomingMessageType;

            for (String messageType : getAllowedMessageTypes()) {
                if (incomingMessageType.equals(messageType)) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new FilterException("Error filtering the message", e);
        }

        return false;
    }
}
