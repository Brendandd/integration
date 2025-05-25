package integration.core.dto;

import integration.core.domain.messaging.OutboxEventType;

/**
 * An outbox event.
 * 
 * @author Brendan Douglas
 * 
 */
public class OutboxEventDto extends BaseDto {
    private static final long serialVersionUID = 5050473264308343049L;

    private long messageFlowId;
    private long componentId;
    private OutboxEventType type;

    public long getMessageFlowId() {
        return messageFlowId;
    }

    public void setMessageFlowId(long messageFlowId) {
        this.messageFlowId = messageFlowId;
    }

    
    public OutboxEventType getType() {
        return type;
    }
    
    
    public long getComponentId() {
        return componentId;
    }
    
    
    public void setComponentId(long componentId) {
        this.componentId = componentId;
    }
    
    
    public void setType(OutboxEventType type) {
        this.type = type;
    }
}
