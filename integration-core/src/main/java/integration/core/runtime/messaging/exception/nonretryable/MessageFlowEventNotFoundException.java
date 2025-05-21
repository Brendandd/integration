package integration.core.runtime.messaging.exception.nonretryable;

import integration.core.exception.ExceptionIdentifierType;
import integration.core.exception.NonRetryableException;

/**
 * An exception which is thrown when an event is not found. This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class MessageFlowEventNotFoundException extends NonRetryableException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    private static String MESSAGE = "Event not found";
    
    public MessageFlowEventNotFoundException(long eventId) {
        super(MESSAGE);
        
        addOtherIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_EVENT_ID, eventId);
    }
}
