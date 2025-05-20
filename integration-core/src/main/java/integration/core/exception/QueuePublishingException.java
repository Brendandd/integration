package integration.core.exception;

import java.util.List;

/**
 * An exception which is thrown during component configuration or reading component configuration.
 * 
 * @author Brendan Douglas
 */
public class QueuePublishingException extends IntegrationException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    public QueuePublishingException(String message, List<ExceptionIdentifier>identifiers, Throwable cause) {
        super(message, identifiers, cause, true);
    }
}
