package integration.core.runtime.messaging.exception.nonretryable;

import integration.core.domain.IdentifierType;
import integration.core.exception.NonRetryableException;

/**
 * An exception which is thrown when a message flow is not found. This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class MessageFlowNotFoundException extends NonRetryableException {
    private static final long serialVersionUID = -6856908339059940620L;
    
    private static String MESSAGE = "Message Flow not found";
    
    public MessageFlowNotFoundException(long messageFlowId) {
        super(MESSAGE);
        
        addOtherIdentifier(IdentifierType.MESSAGE_FLOW_ID, messageFlowId);
    }
}
