package integration.core.runtime.messaging.exception.retryable;

import integration.core.exception.ConditionallyRetryableException;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;

/**
 * An exception which occurs during message flow.  
 * 
 * @author Brendan Douglas
 *
 */
public class MessageFlowProcessingException extends ConditionallyRetryableException {
    private static final long serialVersionUID = -1639485569289392443L;
           
    public MessageFlowProcessingException(String message, long messageFlowId) {
        super(message);
        
        identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId));
    }
    
    
    public MessageFlowProcessingException(String message, long messageFlowId, Throwable cause) {
        super(message, cause);
        
        identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId));
    }
    
    @Override
    public MessageFlowProcessingException addOtherIdentifier(ExceptionIdentifierType type, Object value) {
        super.addOtherIdentifier(type, value);
        return this;
    }
}
