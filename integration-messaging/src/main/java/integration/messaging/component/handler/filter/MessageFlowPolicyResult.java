package integration.messaging.component.handler.filter;

public class MessageFlowPolicyResult {
    private boolean success;
    private String failureReason;
    
    public MessageFlowPolicyResult(boolean success) {
        this.success = success;
    }
    
    public MessageFlowPolicyResult(boolean success, String failureReason) {
        this.success = success;
        this.failureReason = failureReason;
    }

    public boolean isSuccess() {
        return success;
    }

    
    public String getFailureReason() {
        return failureReason;
    }
}
