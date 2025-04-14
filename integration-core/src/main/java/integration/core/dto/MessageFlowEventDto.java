package integration.core.dto;

/**
 * A message flow event.
 * 
 * @author Brendan Douglas
 * 
 */
public class MessageFlowEventDto extends BaseDto {
    private static final long serialVersionUID = 5050473264308343049L;

    private long messageFlowId;

    public long getMessageFlowId() {
        return messageFlowId;
    }

    public void setMessageFlowId(long messageFlowId) {
        this.messageFlowId = messageFlowId;
    }
}
