package integration.core.runtime.messaging.component.type.handler.filter;

import integration.core.runtime.messaging.exception.retryable.MessageFlowServiceProcessingException;

/**
 * An exception thrown from the filtering logic
 */
public class FilterException extends MessageFlowServiceProcessingException {
    private static final long serialVersionUID = -6535976021157034699L;

    public FilterException(String message, long messageFlowId) {
        super(message, messageFlowId);
    }

    
    public FilterException(String message, long messageFlowId, Throwable cause) {
        super(message, messageFlowId,cause);
    }
}
