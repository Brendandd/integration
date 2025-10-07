package integration.core.runtime.messaging.exception.retryable;

import integration.core.domain.IdentifierType;
import integration.core.exception.ConditionallyRetryableException;

/**
 * An exception which is thrown when an event is not found. This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class OutboxEventProcessingException extends ConditionallyRetryableException {
    private static final long serialVersionUID = -4695383656301388320L;


    public OutboxEventProcessingException(String message, long eventId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(IdentifierType.EVENT_ID, eventId);
    }

    
    public OutboxEventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    
    @Override
    public OutboxEventProcessingException addOtherIdentifier(IdentifierType type, Object value) {
        super.addOtherIdentifier(type, value);
        return this;
    }
}
