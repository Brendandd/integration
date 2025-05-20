package integration.core.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * An exception which is thrown when there is an issue accessing a route.
 * 
 * @author Brendan Douglas
 */
public class RouteAccessException extends IntegrationException {
    private static final long serialVersionUID = -8219003265184923387L;
       
    public RouteAccessException(String message, long routeId, Throwable cause) {
        super(message, List.of(new ExceptionIdentifier(ExceptionIdentifierType.ROUTE_ID, routeId)), cause);
    }

    
    public RouteAccessException(String message, Throwable cause) {
        super(message, new ArrayList<>(), cause);
    }
}
