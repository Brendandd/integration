package integration.core.runtime.messaging.exception.retryable;

import integration.core.exception.ConditionallyRetryableException;
import integration.core.exception.ExceptionIdentifierType;

/**
 * An exception which is thrown when an event is not found. This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class MessageFlowEventProcessingException extends ConditionallyRetryableException {
    private static final long serialVersionUID = -8219003265184923387L;
          
    public MessageFlowEventProcessingException(String message, long eventId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_EVENT_ID, eventId);
    }

    
    public MessageFlowEventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    
    @Override
    public MessageFlowEventProcessingException addOtherIdentifier(ExceptionIdentifierType type, Object value) {
        super.addOtherIdentifier(type, value);
        return this;
    }
}
