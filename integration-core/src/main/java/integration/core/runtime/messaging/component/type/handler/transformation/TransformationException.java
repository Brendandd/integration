package integration.core.runtime.messaging.component.type.handler.transformation;

import integration.core.runtime.messaging.exception.retryable.MessageFlowServiceProcessingException;

/**
 * A transformation exception
 * 
 * @author brendan_douglas_a
 *
 */
public class TransformationException extends MessageFlowServiceProcessingException {
    private static final long serialVersionUID = -8122323055739569340L;


    public TransformationException(String message, long messageFlowId) {
        super(message, messageFlowId);
    }

    
    public TransformationException(String message, long messageFlowId, Throwable cause) {
        super(message, messageFlowId, cause);
    }
}
