package integration.core.runtime.messaging.exception.nonretryable;

import integration.core.domain.IdentifierType;

/**
 * An exception which is thrown when a message flow is not found. This type of exception cannot be retried.
 * 
 * @author Brendan Douglas
 */
public class MessageFlowNotFoundException extends EntityNotFoundException {
    private static final long serialVersionUID = -6856908339059940620L;
    
    private final static String ENTITY = "Route";
    
    public MessageFlowNotFoundException(long messageFlowId) {
        super(ENTITY, messageFlowId, IdentifierType.MESSAGE_FLOW_ID);
    }
}
