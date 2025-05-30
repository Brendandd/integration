package integration.core.exception;

import integration.core.domain.IdentifierType;
import integration.core.runtime.messaging.exception.nonretryable.EntityNotFoundException;

/**
 * An exception which is thrown when a component is not found. This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class ComponentNotFoundException extends EntityNotFoundException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    private final static String ENTITY = "Component";
    
    public ComponentNotFoundException(long componentId) {
        super(ENTITY, componentId, IdentifierType.COMPONENT_ID);
    }
}
