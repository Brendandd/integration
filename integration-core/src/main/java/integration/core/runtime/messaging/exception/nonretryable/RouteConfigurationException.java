package integration.core.runtime.messaging.exception.nonretryable;

import integration.core.domain.IdentifierType;

/**
 * An exception which is thrown when there is an issue with the route and component annotations.  This type of exception will occur at startup and not during message flow.
 * 
 * @author Brendan Douglas
 */
public class RouteConfigurationException extends ConfigurationException {
    private static final long serialVersionUID = 1071927279791183591L;


    public RouteConfigurationException(String message, long routeId) {
        super(message);
        
        addOtherIdentifier(IdentifierType.ROUTE_ID, routeId);
    }

    
    public RouteConfigurationException(String message, long routeId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(IdentifierType.ROUTE_ID, routeId);
    }
}
