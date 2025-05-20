package integration.core.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * An exception which is thrown when there is an issue accessing a component.
 * 
 * @author Brendan Douglas
 */
public class ComponentAccessException extends IntegrationException {
    private static final long serialVersionUID = -8219003265184923387L;
       
    public ComponentAccessException(String message, long componentId, Throwable cause) {
        super(message, List.of(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId)), cause);
    }

    
    public ComponentAccessException(String message, Throwable cause) {
        super(message, new ArrayList<>(), cause);
    }
}
