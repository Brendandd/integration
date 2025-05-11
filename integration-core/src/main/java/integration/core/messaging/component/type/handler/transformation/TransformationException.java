package integration.core.messaging.component.type.handler.transformation;

import java.util.ArrayList;

import integration.core.messaging.MessageFlowException;

/**
 * A transformation exception
 * 
 * @author brendan_douglas_a
 *
 */
public class TransformationException extends MessageFlowException {
    private static final long serialVersionUID = -8122323055739569340L;


    public TransformationException(String message, long messageFlowId, boolean isRetryable) {
        super(message, messageFlowId, new ArrayList<>(), isRetryable);
    }

    
    public TransformationException(String message, long messageFlowId, Throwable cause) {
        super(message, messageFlowId, new ArrayList<>(), cause);
    }
}
