package integration.core.exception;

import integration.core.domain.IdentifierType;

/**
 * An exception which is thrown when a component is not found. This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class ComponentNotFoundException extends NonRetryableException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    private static String MESSAGE = "Component not found";
    
    public ComponentNotFoundException(long componentId) {
        super(MESSAGE);
        
        addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
    }
}
