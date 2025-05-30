package integration.core.runtime.messaging.exception.nonretryable;

import integration.core.domain.IdentifierType;

/**
 * An exception which is thrown when an outbox event is not found. This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class OutboxEventNotFoundException extends EntityNotFoundException {
    private static final long serialVersionUID = -448389444835675541L;
    
    private final static String ENTITY = "Outbox event";
    
    public OutboxEventNotFoundException(long eventId) {
        super(ENTITY, eventId, IdentifierType.OUTBOX_EVENT_ID);
    }
}
