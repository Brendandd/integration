package integration.core.dto;

/**
 * Details about the current flow step.
 * 
 * @author Brendan Douglas
 */
public class MessageFlowStepDto extends BaseDto {
    private static final long serialVersionUID = -7900743785077406998L;

    private MessageDto message;

    
    public MessageDto getMessage() {
        return message;
    }
    
    
    public void setMessage(MessageDto message) {
        this.message = message;
    }

    public String getMessageContent() {
        return message.getContent();
    }
}
