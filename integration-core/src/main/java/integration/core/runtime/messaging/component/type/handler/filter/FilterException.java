package integration.core.runtime.messaging.component.type.handler.filter;

import java.util.ArrayList;

import integration.core.runtime.messaging.exception.MessageFlowException;

/**
 * An exception thrown from the filtering logic
 */
public class FilterException extends MessageFlowException {
    private static final long serialVersionUID = -6535976021157034699L;

    public FilterException(String message, long messageFlowId, boolean isRetryable) {
        super(message, messageFlowId, new ArrayList<>(), isRetryable);
    }

    
    public FilterException(String message, long messageFlowId, Throwable cause) {
        super(message, messageFlowId, new ArrayList<>(), cause);
    }
}
