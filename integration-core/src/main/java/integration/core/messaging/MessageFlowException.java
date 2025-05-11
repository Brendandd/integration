package integration.core.messaging;

import java.util.List;

import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.exception.IntegrationException;

/**
 * An exception which occurs during message flow.  
 * 
 * @author Brendan Douglas
 *
 */
public class MessageFlowException extends IntegrationException {
    private static final long serialVersionUID = -1639485569289392443L;
           
    public MessageFlowException(String message, long messageFlowId, List<ExceptionIdentifier>otherIdentifiers, boolean isRetryable) {
        super(message, otherIdentifiers, isRetryable);
        
        identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId));
    }

    
    public MessageFlowException(String message, long messageFlowId, List<ExceptionIdentifier>otherIdentifiers, Throwable cause) {
        super(message, otherIdentifiers, cause);
        
        identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId));
    }
    
    
    public MessageFlowException(String message, long messageFlowId, List<ExceptionIdentifier>otherIdentifiers, Throwable cause, boolean isRetryable) {
        super(message, otherIdentifiers, cause, isRetryable);
        
        identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId));
    }

    
    public MessageFlowException(String message, List<ExceptionIdentifier>otherIdentifiers, Throwable cause, boolean isRetryable) {
        super(message, otherIdentifiers, cause, isRetryable);
    }
}
