package integration.core.exception;

import java.util.List;

/**
 * An exception which is thrown during component configuration or reading component configuration.
 * 
 * @author Brendan Douglas
 */
public class ConfigurationException extends IntegrationException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    public ConfigurationException(String message, List<ExceptionIdentifier>identifiers, boolean isRetryable) {
        super(message, identifiers, isRetryable);
    }

    
    public ConfigurationException(String message, List<ExceptionIdentifier>identifiers, Throwable cause) {
        super(message, identifiers, cause);
    }
}
