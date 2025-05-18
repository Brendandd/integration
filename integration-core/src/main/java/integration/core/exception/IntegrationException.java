package integration.core.exception;

import java.util.List;

import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;

/**
 * Base class for all custom exception types.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class IntegrationException extends Exception {
    private static final long serialVersionUID = -1639485569289392443L;
        
    protected final List<ExceptionIdentifier> identifiers;
    protected boolean isRetryable;

    
    public IntegrationException(String message, List<ExceptionIdentifier>identifiers, boolean isRetryable) {
        super(message);
        
        this.identifiers = identifiers;
        this.isRetryable = isRetryable;
    }

    
    public IntegrationException(String message, List<ExceptionIdentifier>identifiers, Throwable cause) {
        super(message, cause);
        
        this.identifiers = identifiers;
        
        this.isRetryable = isRetryableCause(cause);
    }

    
    public IntegrationException(String message, List<ExceptionIdentifier>identifiers, Throwable cause, boolean isRetryable) {
        super(message, cause);
        
        this.identifiers = identifiers;
        this.isRetryable = isRetryable;
    }

    
    protected static boolean isRetryableCause(Throwable cause) {
        while (cause != null) {
            if (cause instanceof TransientDataAccessException) {
                return true;
            }
            
            if (cause instanceof RecoverableDataAccessException) {
                return true;
            } 
            
            if (cause instanceof IntegrationException) {
                return ((IntegrationException)cause).isRetryable;
            }
            
            
            cause = cause.getCause();
        }
            
        return false;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append(": ").append(getMessage());
        sb.append(" | Retryable: ").append(isRetryable);
        
        if (identifiers != null && !identifiers.isEmpty()) {
            sb.append(" | Identifiers: [");
            
            for (int i = 0; i < identifiers.size(); i++) {
                ExceptionIdentifier id = identifiers.get(i);
                sb.append(id.getType()).append("=").append(id.getValue());
                if (i < identifiers.size() - 1) {
                    sb.append(", ");
                }
            }
            
            sb.append("]");
        }

        
        Throwable root = getCause();
        if (root != null) {
            sb.append(" | Cause: ").append(root.getClass().getSimpleName()).append(": ").append(root.getMessage());

            // Traverse to the root cause
            Throwable next = root.getCause();
            while (next != null && next != root) {
                root = next;
                next = root.getCause();
            }

            if (root != getCause()) {
                sb.append(" | Root Cause: ").append(root.getClass().getSimpleName()).append(": ").append(root.getMessage());
            }
        }

        return sb.toString();
    }


    public boolean isRetryable() {
        return isRetryable;
    }

    
    public Object getIdentifierValue(ExceptionIdentifierType type) {
        for (ExceptionIdentifier identifier : identifiers) {
            if (identifier.getType() == type) {
                return identifier.getValue();
            }
        }
        
        return null;
    }

    
    public boolean hasIdentifier(ExceptionIdentifierType type) {
        for (ExceptionIdentifier identifier : identifiers) {
            if (identifier.getType() == type) {
                return true;
            }
        }
        
        return false;
    }
}
