package integration.core.exception;

/**
 * An exception thrown when an exception is thrown processing an event from the outbox.
 * 
 * @author Brendan Douglas
 */
public class EventProcessingException extends RuntimeException {
    private long eventId;
    private long messageFlowId;
    private String componentPath;

    private static final long serialVersionUID = -8219003265184923387L;

    public EventProcessingException(String message, long eventId, long messageFlowId, String componentPath, Throwable cause) {
        super(message, cause);
        
        this.eventId = eventId;
        this.messageFlowId = messageFlowId;
        this.componentPath = componentPath;
    }
    
    
    @Override
    public String toString() {
        return "EventDispatchException{" +
               "message='" + getMessage() + '\'' +
               ", eventId=" + eventId +
               ", messageFlowId=" + messageFlowId +
               ", componentPath='" + componentPath + '\'' +
               ", cause=" + (getCause() != null ? getCause().toString() : "null") +
               '}';
    }
}
