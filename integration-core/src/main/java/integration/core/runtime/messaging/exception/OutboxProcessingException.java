package integration.core.runtime.messaging.exception;

import java.util.List;

import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.exception.IntegrationException;

/**
 * An exception which occurs when processing messages in the outrbox.  All exceptions caught in the outbox processing are caught and rethrown as this type of exception.  
 * 
 * @author Brendan Douglas
 *
 */
public class OutboxProcessingException extends IntegrationException {
    private static final long serialVersionUID = -1639485569289392443L;
           
    public OutboxProcessingException(String message, long eventId, List<ExceptionIdentifier>otherIdentifiers) {
        super(message, otherIdentifiers, true);
        
        otherIdentifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.EVENT_ID, eventId));
    }

    
    public OutboxProcessingException(String message, long componentId, Throwable cause) {
        super(message, List.of(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId)), cause, true);
    }

    
    public OutboxProcessingException(String message, List<ExceptionIdentifier>otherIdentifiers) {
        super(message, otherIdentifiers, true);
    }

    
    public OutboxProcessingException(String message, List<ExceptionIdentifier>otherIdentifiers, Throwable cause) {
        super(message, otherIdentifiers, cause, true);
    }
}
