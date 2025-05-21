package integration.core.runtime.messaging.exception.retryable;

import integration.core.exception.ConditionallyRetryableException;
import integration.core.exception.ExceptionIdentifierType;

/**
 * An exception which is thrown during when a components forwards a message.
 * 
 * @author Brendan Douglas
 */
public class MessageForwardingException extends ConditionallyRetryableException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    public MessageForwardingException(String message, long componentId) {
        super(message);
        
        addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
    }

    
    public MessageForwardingException(String message, long componentId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
    }
}
