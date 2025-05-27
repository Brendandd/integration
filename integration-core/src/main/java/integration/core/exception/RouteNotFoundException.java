package integration.core.exception;

import integration.core.domain.IdentifierType;
import integration.core.runtime.messaging.exception.nonretryable.EntityNotFoundException;

/**
 * An exception which is thrown when a route is not found.  This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class RouteNotFoundException extends EntityNotFoundException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    private static String ENTITY = "Route";
    
    public RouteNotFoundException(long routeId) {
        super(ENTITY, routeId, IdentifierType.ROUTE_ID);
    }
}
