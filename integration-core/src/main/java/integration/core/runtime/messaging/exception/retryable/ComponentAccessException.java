package integration.core.runtime.messaging.exception.retryable;

import integration.core.exception.ConditionallyRetryableException;
import integration.core.exception.ExceptionIdentifierType;

/**
 * An exception which is thrown when there is an issue accessing a component.
 * 
 * @author Brendan Douglas
 */
public class ComponentAccessException extends ConditionallyRetryableException {
    private static final long serialVersionUID = -8219003265184923387L;
       
    public ComponentAccessException(String message, long componentId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
    }

    
    public ComponentAccessException(String message, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(ExceptionIdentifierType.NO_ID, null);
    }
}
