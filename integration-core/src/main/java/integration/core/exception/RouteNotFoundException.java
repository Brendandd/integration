package integration.core.exception;

/**
 * An exception which is thrown when a route is not found.  This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class RouteNotFoundException extends NonRetryableException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    private static String MESSAGE = "Route not found";
    
    public RouteNotFoundException(long routeId) {
        super(MESSAGE);
        
        addOtherIdentifier(ExceptionIdentifierType.ROUTE_ID, routeId);
    }
}
