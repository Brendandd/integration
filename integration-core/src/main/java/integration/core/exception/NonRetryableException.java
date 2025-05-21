package integration.core.exception;

/**
 * Base class for all non  retryable exceptions.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class NonRetryableException extends IntegrationException {
    private static final long serialVersionUID = -1639485569289392443L;
       
    public NonRetryableException(String message) {
        super(message);
    }

    public NonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
