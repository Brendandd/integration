package integration.messaging.hl7.component.handler.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.type.handler.filter.FilterException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageAcceptancePolicy;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * Base class for hl7 message type filters. This type of filter will
 * accept/reject the message based on the type of the hl7 message.
 * 
 * @author Brendan Douglas
 */
public abstract class MessageTypeFilter extends MessageAcceptancePolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageTypeFilter.class);
    
    public abstract String[] getAllowedMessageTypes();
    
    protected abstract String getFilteredReason();
    

    @Override
    public MessageFlowPolicyResult applyPolicy(MessageFlowDto messageFlow) throws FilterException {

        try {
            HL7Message source = new HL7Message(messageFlow.getMessage().getContent());
            
            String incomingMessageType = source.getMessageTypeField().value();

            for (String messageType : getAllowedMessageTypes()) {
                if (incomingMessageType.equals(messageType)) {
                    return new MessageFlowPolicyResult(true);
                }
            }
        } catch (Exception e) {
            throw new FilterException("Error filtering the message", messageFlow.getId(), e);
        }

        return new MessageFlowPolicyResult(false, getName(), getFilteredReason());
    }
}
