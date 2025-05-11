package integration.core.messaging;

import java.util.List;

import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.exception.IntegrationException;

/**
 * An exception which occurs during message flow.  
 * 
 * @author Brendan Douglas
 *
 */
public class EventProcessingException extends IntegrationException {
    private static final long serialVersionUID = -1639485569289392443L;
           
    public EventProcessingException(String message, long eventId, List<ExceptionIdentifier>otherIdentifiers, boolean isRetryable) {
        super(message, otherIdentifiers, isRetryable);
        
        otherIdentifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.EVENT_ID, eventId));
    }

    
    public EventProcessingException(String message, long eventId, List<ExceptionIdentifier>otherIdentifiers, Throwable cause) {
        super(message, otherIdentifiers, cause);
        
        identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.EVENT_ID, eventId));
    }

    
    public EventProcessingException(String message, long eventId, List<ExceptionIdentifier>otherIdentifiers, Throwable cause, boolean isRetryable) {
        super(message, otherIdentifiers, cause, isRetryable);
        
        identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.EVENT_ID, eventId));
    }

    
    public EventProcessingException(String message, List<ExceptionIdentifier>otherIdentifiers, boolean isRetryable) {
        super(message, otherIdentifiers, isRetryable);
    }

    
    public EventProcessingException(String message, List<ExceptionIdentifier>otherIdentifiers, Throwable cause) {
        super(message, otherIdentifiers, cause);
    }
    
    
    public EventProcessingException(String message, List<ExceptionIdentifier>otherIdentifiers, Throwable cause, boolean isRetryable) {
        super(message, otherIdentifiers, cause, isRetryable);
    }
}
