package integration.core.runtime.messaging.exception.retryable;

import integration.core.domain.IdentifierType;
import integration.core.exception.ConditionallyRetryableException;

/**
 * An exception which is thrown when there is an issue accessing a component.
 * 
 * @author Brendan Douglas
 */
public class ComponentAccessException extends ConditionallyRetryableException {
    private static final long serialVersionUID = 2605794089589297929L;


    public ComponentAccessException(String message, long componentId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
    }

    
    public ComponentAccessException(String message, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(IdentifierType.NO_ID, null);
    }
}
