package integration.core.runtime.messaging.exception.retryable;

import integration.core.domain.IdentifierType;
import integration.core.exception.ConditionallyRetryableException;
import integration.core.exception.ExceptionIdentifier;

/**
 * An exception which occurs during message flow in the service layer.  
 * 
 * @author Brendan Douglas
 *
 */
public class MessageFlowProcessingException extends ConditionallyRetryableException {
    private static final long serialVersionUID = 8667963670626591735L;

    public MessageFlowProcessingException(String message, long messageFlowId) {
        super(message);
        
        identifiers.add(new ExceptionIdentifier(IdentifierType.MESSAGE_FLOW_ID, messageFlowId));
    }
    
    
    public MessageFlowProcessingException(String message, long messageFlowId, Throwable cause) {
        super(message, cause);
        
        identifiers.add(new ExceptionIdentifier(IdentifierType.MESSAGE_FLOW_ID, messageFlowId));
    }
    
    @Override
    public MessageFlowProcessingException addOtherIdentifier(IdentifierType type, Object value) {
        super.addOtherIdentifier(type, value);
        return this;
    }
}
