package integration.core.exception;

/**
 * Base class for all retryable exceptions.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class ConditionallyRetryableException extends IntegrationException {
    private static final long serialVersionUID = -1639485569289392443L;
       
    public ConditionallyRetryableException(String message) {
        super(message);
    }

    
    public ConditionallyRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
