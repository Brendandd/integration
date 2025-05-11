package integration.core.messaging.component.type.handler.filter;

public class MessageFlowPolicyResult {
    private boolean success;
    private String filterReason;
    private String filterName;
    
    public MessageFlowPolicyResult(boolean success) {
        this.success = success;
    }
    
    public MessageFlowPolicyResult(boolean success, String filterName, String filterReason) {
        this.success = success;
        this.filterReason = filterReason;
        this.filterName = filterName;
    }

    public boolean isSuccess() {
        return success;
    }

    
    public String getFilterReason() {
        return filterReason;
    }

    public String getFilterName() {
        return filterName;
    }
}
