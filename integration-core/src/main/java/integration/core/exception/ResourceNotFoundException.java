package integration.core.exception;

import java.util.List;

/**
 * An exception which is thrown during component configuration or reading component configuration.
 * 
 * @author Brendan Douglas
 */
public class ResourceNotFoundException extends ConfigurationException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    public ResourceNotFoundException(String message, List<ExceptionIdentifier>identifiers, boolean isRetryable) {
        super(message, identifiers, isRetryable);
    }

    
    public ResourceNotFoundException(String message, List<ExceptionIdentifier>identifiers, Throwable cause) {
        super(message, identifiers, cause);
    }
}
