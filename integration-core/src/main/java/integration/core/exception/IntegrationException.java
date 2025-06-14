package integration.core.exception;

import java.net.ConnectException;
import java.sql.SQLTransientException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;

import integration.core.domain.IdentifierType;

/**
 * Base class for all custom exception types.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class IntegrationException extends Exception {
    private static final long serialVersionUID = -1639485569289392443L;
        
    protected final List<ExceptionIdentifier> identifiers = new ArrayList<>();
    
    public IntegrationException(String message) {
        super(message);
    }

    
    public IntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    
    private boolean isExplicitlyRetryableException(Throwable t) {
        return t instanceof TransientDataAccessException ||
               t instanceof RecoverableDataAccessException ||
               t instanceof SQLTransientException || 
               t instanceof ConnectException;
    }
    
    public boolean isRetryable() {
        Throwable current = this;

        while (current != null) {
            if (current instanceof NonRetryableException) {
                return false;
            }

            if (isExplicitlyRetryableException(current)) {
                return true;
            }

            if (current instanceof IntegrationException && current != this) {
                return ((IntegrationException) current).isRetryable();
            }

            current = current.getCause();
        }

        return false;
    }

    
    public IntegrationException addOtherIdentifier(IdentifierType type, Object value) {
        identifiers.add(new ExceptionIdentifier(type, value));
        return this;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append(": ").append(getMessage());
        sb.append(" | Retryable: ").append(isRetryable());
        
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

    
    public Object getIdentifierValue(IdentifierType type) {
        for (ExceptionIdentifier identifier : identifiers) {
            if (identifier.getType() == type) {
                return identifier.getValue();
            }
        }
        
        return null;
    }

    
    public boolean hasIdentifier(IdentifierType type) {
        for (ExceptionIdentifier identifier : identifiers) {
            if (identifier.getType() == type) {
                return true;
            }
        }
        
        return false;
    }
}
