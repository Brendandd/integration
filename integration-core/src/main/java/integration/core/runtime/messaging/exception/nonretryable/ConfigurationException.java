package integration.core.runtime.messaging.exception.nonretryable;

import integration.core.exception.NonRetryableException;

/**
 * An exception which is thrown during app startup
 * 
 * @author Brendan Douglas
 */
public class ConfigurationException extends NonRetryableException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    public ConfigurationException(String message) {
        super(message);
    }

    
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
