package integration.core.runtime.messaging.exception.retryable;

import integration.core.domain.IdentifierType;
import integration.core.exception.ConditionallyRetryableException;

/**
 * An exception which is thrown when there is an issue with the outbox event scheduler.
 * 
 * @author Brendan Douglas
 */
public class OutboxEventSchedulerException extends ConditionallyRetryableException {
    private static final long serialVersionUID = 126025094202942735L;

    public OutboxEventSchedulerException(long componentId, Throwable cause) {
        super("Error processing events from the inbox", cause);
        
        addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
    }
}
