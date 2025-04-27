package integration.messaging.hl7.component.handler.filter;

import integration.core.dto.MessageFlowStepDto;
import integration.messaging.component.handler.filter.FilterException;
import integration.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * Base class for hl7 message type filters. This type of filter will
 * accept/reject the message based on the type of the hl7 message.
 * 
 * @author Brendan Douglas
 */
public abstract class MessageTypeFilter extends MessageAcceptancePolicy {
    public abstract String[] getAllowedMessageTypes();

    protected String messageType = null;

    @Override
    public boolean applyPolicy(MessageFlowStepDto messageFlowStep) throws FilterException {

        try {
            HL7Message source = new HL7Message(messageFlowStep.getMessage().getContent());
            
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
