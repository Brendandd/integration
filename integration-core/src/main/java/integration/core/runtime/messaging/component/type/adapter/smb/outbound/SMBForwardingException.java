package integration.core.runtime.messaging.component.type.adapter.smb.outbound;

import integration.core.domain.IdentifierType;
import integration.core.exception.ConditionallyRetryableException;

/**
 * An exception which is thrown during when a components fails to forward a message. Forwarding a message occurs during outbox processing so the primary id is the event Id.
 * 
 * @author Brendan Douglas
 */
public class SMBForwardingException extends ConditionallyRetryableException {
    private static final long serialVersionUID = 9128189555450069980L;

    public SMBForwardingException(long eventId, long componentId, long messageFlowId, Throwable cause){
        super("Exception forwarding message via SMB", cause);
        
        addOtherIdentifier(IdentifierType.OUTBOX_EVENT_ID, eventId);
        addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
        addOtherIdentifier(IdentifierType.MESSAGE_FLOW_ID, messageFlowId);
    }
}
