package integration.core.exception;

import java.util.List;

/**
 * An exception which is thrown when a component is not found
 * 
 * @author Brendan Douglas
 */
public class ComponentNotFoundException extends ConfigurationException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    private static String MESSAGE = "Component not found";
    
    public ComponentNotFoundException(long componentId) {
        super(MESSAGE, List.of(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId)),false);
    }
}
