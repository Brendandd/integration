package integration.core.runtime.messaging.exception.nonretryable;

import integration.core.exception.ExceptionIdentifierType;

/**
 * An exception which is thrown when there is an issue with the route and component annotations.  This type of exception will occur at startup and not during message flow.
 * 
 * @author Brendan Douglas
 */
public class RouteConfigurationException extends ConfigurationException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    public RouteConfigurationException(String message, long routeId) {
        super(message);
        
        addOtherIdentifier(ExceptionIdentifierType.ROUTE_ID, routeId);
    }

    
    public RouteConfigurationException(String message, long routeId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(ExceptionIdentifierType.ROUTE_ID, routeId);
    }
}
