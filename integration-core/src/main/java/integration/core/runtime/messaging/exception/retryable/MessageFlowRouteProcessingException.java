package integration.core.runtime.messaging.exception.retryable;

import integration.core.exception.ConditionallyRetryableException;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;

/**
 * An exception which occurs during message flow in the Camel routes.  
 * 
 * @author Brendan Douglas
 *
 */
public class MessageFlowRouteProcessingException extends ConditionallyRetryableException {
    private static final long serialVersionUID = -1639485569289392443L;
           
    public MessageFlowRouteProcessingException(String message, long messageFlowId) {
        super(message);
        
        identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId));
    }
    
    
    public MessageFlowRouteProcessingException(String message, long messageFlowId, Throwable cause) {
        super(message, cause);
        
        identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId));
    }
    
    @Override
    public MessageFlowRouteProcessingException addOtherIdentifier(ExceptionIdentifierType type, Object value) {
        super.addOtherIdentifier(type, value);
        return this;
    }
}
