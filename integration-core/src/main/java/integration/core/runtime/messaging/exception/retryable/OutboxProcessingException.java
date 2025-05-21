package integration.core.runtime.messaging.exception.retryable;

import integration.core.exception.ConditionallyRetryableException;
import integration.core.exception.ExceptionIdentifierType;

/**
 * An exception which occurs when processing messages in the outrbox.  All exceptions caught in the outbox processing are caught and rethrown as this type of exception.  
 * 
 * @author Brendan Douglas
 *
 */
public class OutboxProcessingException extends ConditionallyRetryableException {
    private static final long serialVersionUID = -1639485569289392443L;
           
    public OutboxProcessingException(String message, long componentId) {
        super(message);
        
        addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
    }

    
    public OutboxProcessingException(String message, long componentId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
    }
}
