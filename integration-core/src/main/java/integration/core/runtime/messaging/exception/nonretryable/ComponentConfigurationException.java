package integration.core.runtime.messaging.exception.nonretryable;

import integration.core.exception.ExceptionIdentifierType;

/**
 * An exception which is thrown when there is an issue with the route and component annotations.  This type of exception will occur at startup and not during message flow.
 * 
 * @author Brendan Douglas
 */
public class ComponentConfigurationException extends ConfigurationException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    public ComponentConfigurationException(String message, long componentId) {
        super(message);
        
        addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
    }

    
    public ComponentConfigurationException(String message, long componentId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
    }
}
