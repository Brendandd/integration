package integration.core.dto;

import integration.core.domain.messaging.MessageFlowEventType;

/**
 * A message flow event.
 * 
 * @author Brendan Douglas
 * 
 */
public class MessageFlowEventDto extends BaseDto {
    private static final long serialVersionUID = 5050473264308343049L;

    private long messageFlowId;
    private long componentId;
    private MessageFlowEventType type;

    public long getMessageFlowId() {
        return messageFlowId;
    }

    public void setMessageFlowId(long messageFlowId) {
        this.messageFlowId = messageFlowId;
    }

    
    public MessageFlowEventType getType() {
        return type;
    }
    
    
    public long getComponentId() {
        return componentId;
    }
    
    
    public void setComponentId(long componentId) {
        this.componentId = componentId;
    }
    
    
    public void setType(MessageFlowEventType type) {
        this.type = type;
    }
}
