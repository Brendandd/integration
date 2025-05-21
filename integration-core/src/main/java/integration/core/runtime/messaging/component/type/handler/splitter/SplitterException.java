package integration.core.runtime.messaging.component.type.handler.splitter;

import integration.core.runtime.messaging.exception.retryable.MessageFlowServiceProcessingException;

/**
 * A message splitter exception
 * 
 * @author brendan_douglas_a
 *
 */
public class SplitterException extends MessageFlowServiceProcessingException {

    private static final long serialVersionUID = 200520031395049131L;

    public SplitterException(String message, long messageFlowId) {
        super(message, messageFlowId);
    }

    
    public SplitterException(String message, long messageFlowId, Throwable cause) {
        super(message, messageFlowId, cause);
    }
}
