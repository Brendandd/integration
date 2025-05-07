package integration.core.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Details about the current flow.
 * 
 * @author Brendan Douglas
 */
public class MessageFlowDto extends BaseDto {
    private static final long serialVersionUID = -7900743785077406998L;

    private MessageDto message;
    private List<MessageFlowPropertyDto>properties = new ArrayList<>();

    
    public MessageDto getMessage() {
        return message;
    }
    
    
    public void setMessage(MessageDto message) {
        this.message = message;
    }

    public String getMessageContent() {
        return message.getContent();
    }


    public List<MessageFlowPropertyDto> getProperties() {
        return properties;
    }


    public void setProperties(List<MessageFlowPropertyDto> properties) {
        this.properties = properties;
    }
    
    
    public void addProperty(MessageFlowPropertyDto property) {
        
    }
}
