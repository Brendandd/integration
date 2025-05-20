package integration.core.exception;

import java.util.List;

/**
 * An exception which is thrown when an event is not found
 * 
 * @author Brendan Douglas
 */
public class EventNotFoundException extends IntegrationException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    private static String MESSAGE = "Event not found. Id: ";
    
    public EventNotFoundException(long eventId) {
        super(MESSAGE + eventId , List.of(new ExceptionIdentifier(ExceptionIdentifierType.ROUTE_ID, eventId)), false);
    }
}
