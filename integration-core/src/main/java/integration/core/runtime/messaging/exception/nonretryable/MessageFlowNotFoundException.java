package integration.core.runtime.messaging.exception.nonretryable;

import integration.core.exception.ExceptionIdentifierType;
import integration.core.exception.NonRetryableException;

/**
 * An exception which is thrown when a message flow is not found. This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class MessageFlowNotFoundException extends NonRetryableException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    private static String MESSAGE = "Message Flow not found";
    
    public MessageFlowNotFoundException(long messageFlowId) {
        super(MESSAGE);
        
        addOtherIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId);
    }
}
