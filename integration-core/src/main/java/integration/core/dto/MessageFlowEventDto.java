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
    private String owner;
    private String componentPath;

    public long getMessageFlowId() {
        return messageFlowId;
    }

    public void setMessageFlowId(long messageFlowId) {
        this.messageFlowId = messageFlowId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }
}
