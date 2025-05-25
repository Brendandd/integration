package integration.core.runtime.messaging.exception.retryable;

import integration.core.domain.IdentifierType;
import integration.core.exception.ConditionallyRetryableException;

/**
 * An exception which is thrown during component configuration or reading component configuration.
 * 
 * @author Brendan Douglas
 */
public class QueuePublishingException extends ConditionallyRetryableException {
    private static final long serialVersionUID = -9196510827970813141L;

    public QueuePublishingException(String message, long eventId, long componentId, long messageFlowId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(IdentifierType.OUTBOX_EVENT_ID, eventId);
        addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
        addOtherIdentifier(IdentifierType.MESSAGE_FLOW_ID, messageFlowId);
    }
}
