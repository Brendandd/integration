package integration.core.runtime.messaging.exception.retryable;

import integration.core.exception.ConditionallyRetryableException;
import integration.core.exception.ExceptionIdentifierType;

/**
 * An exception which is thrown during when a components fails to forward a message. Forwarding a message occurs during outbox processing so the primary id is the event Id.
 * 
 * @author Brendan Douglas
 */
public class MessageForwardingException extends ConditionallyRetryableException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    public MessageForwardingException(String message, long eventId, long componentId, long messageFlowId, Throwable cause){
        super(message, cause);
        
        addOtherIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_EVENT_ID, eventId);
        addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
        addOtherIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId);
    }
}
