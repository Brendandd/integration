package integration.core.runtime.messaging.exception.retryable;

import integration.core.exception.ConditionallyRetryableException;
import integration.core.exception.ExceptionIdentifierType;

/**
 * An exception which is thrown during component configuration or reading component configuration.
 * 
 * @author Brendan Douglas
 */
public class QueuePublishingException extends ConditionallyRetryableException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    public QueuePublishingException(String message, long eventId, long componentId, long messageFlowId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_EVENT_ID, eventId);
        addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
        addOtherIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId);
    }
}
