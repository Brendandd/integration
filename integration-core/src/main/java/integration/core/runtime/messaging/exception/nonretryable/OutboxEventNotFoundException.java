package integration.core.runtime.messaging.exception.nonretryable;

import integration.core.domain.IdentifierType;
import integration.core.exception.NonRetryableException;

/**
 * An exception which is thrown when an event is not found. This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class OutboxEventNotFoundException extends NonRetryableException {
    private static final long serialVersionUID = -448389444835675541L;
    
    private static String MESSAGE = "Event not found";
    
    public OutboxEventNotFoundException(long eventId) {
        super(MESSAGE);
        
        addOtherIdentifier(IdentifierType.OUTBOX_EVENT_ID, eventId);
    }
}
