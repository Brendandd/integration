package integration.core.runtime.messaging.component.type.handler.splitter;

import java.util.ArrayList;

import integration.core.runtime.messaging.exception.MessageFlowException;

/**
 * A message splitter exception
 * 
 * @author brendan_douglas_a
 *
 */
public class SplitterException extends MessageFlowException {

    private static final long serialVersionUID = 200520031395049131L;

    public SplitterException(String message, long messageFlowId, boolean isRetryable) {
        super(message, messageFlowId, new ArrayList<>(), isRetryable);
    }

    
    public SplitterException(String message, long messageFlowId, Throwable cause) {
        super(message, messageFlowId, new ArrayList<>(), cause);
    }
}
