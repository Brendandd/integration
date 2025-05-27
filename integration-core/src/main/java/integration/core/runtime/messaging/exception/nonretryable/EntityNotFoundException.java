package integration.core.runtime.messaging.exception.nonretryable;

import integration.core.domain.IdentifierType;
import integration.core.exception.NonRetryableException;

/**
 * Base class for all entity not found exceptions.
 * 
 * @author Brendan Douglas
 */
public abstract class EntityNotFoundException extends NonRetryableException {
    private static final long serialVersionUID = -6856908339059940620L;
    
   
    public EntityNotFoundException(String message, long entityId, IdentifierType identifierType) {
        super(message + " not found");
        
        addOtherIdentifier(identifierType, entityId);
    }
}
