package integration.core.runtime.messaging.exception.retryable;

import integration.core.domain.IdentifierType;
import integration.core.exception.ConditionallyRetryableException;

/**
 * An exception which is thrown when there is an issue with the outbox event scheduler.
 * 
 * @author Brendan Douglas
 */
public class InboxEventSchedulerException extends ConditionallyRetryableException {
    private static final long serialVersionUID = 581006761016808349L;

    public InboxEventSchedulerException(long componentId, long messageFlowId, Throwable cause) {
        super("Error processing events from the inbox", cause);
        
        addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
        addOtherIdentifier(IdentifierType.MESSAGE_FLOW_ID, messageFlowId);
    }
}
