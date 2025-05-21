package integration.core.runtime.messaging.exception.retryable;

import integration.core.exception.ConditionallyRetryableException;
import integration.core.exception.ExceptionIdentifierType;

/**
 * An exception which is thrown when there is an issue accessing a route.
 * 
 * @author Brendan Douglas
 */
public class RouteAccessException extends ConditionallyRetryableException {
    private static final long serialVersionUID = -8219003265184923387L;
       
    public RouteAccessException(String message, long routeId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(ExceptionIdentifierType.ROUTE_ID, routeId);
    }
    
    
    public RouteAccessException(String message, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(ExceptionIdentifierType.NO_ID, null);
    }
}
