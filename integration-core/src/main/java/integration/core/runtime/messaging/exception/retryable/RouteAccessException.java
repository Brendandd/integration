package integration.core.runtime.messaging.exception.retryable;

import integration.core.domain.IdentifierType;
import integration.core.exception.ConditionallyRetryableException;

/**
 * An exception which is thrown when there is an issue accessing a route.
 * 
 * @author Brendan Douglas
 */
public class RouteAccessException extends ConditionallyRetryableException {
    private static final long serialVersionUID = 1776033176707844098L;


    public RouteAccessException(String message, long routeId, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(IdentifierType.ROUTE_ID, routeId);
    }
    
    
    public RouteAccessException(String message, Throwable cause) {
        super(message, cause);
        
        addOtherIdentifier(IdentifierType.NO_ID, null);
    }
}
