package integration.core.dto;

/**
 * Details about the current flow step.
 * 
 * @author Brendan Douglas
 */
public class MessageFlowStepDto extends BaseDto {
    private static final long serialVersionUID = -7900743785077406998L;

    private String messageContent;
    private long messageFlowId;

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    @Override
    public String toString() {
        return messageContent;
    }

    public long getMessageFlowId() {
        return messageFlowId;
    }

    public void setMessageFlowId(long messageFlowId) {
        this.messageFlowId = messageFlowId;
    }
}
