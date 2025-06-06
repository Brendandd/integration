package integration.core.dto;

public class MessageFlowPropertyDto extends BaseDto {
    private String key;
    private Object value;
    
    public MessageFlowPropertyDto() {
        
    }
    
    public MessageFlowPropertyDto(String key, Object value) {
        this.key = key;
        this.value = value;
    }
    
    
    public String getKey() {
        return key;
    }
    
    
    public void setKey(String key) {
        this.key = key;
    }
    
    
    public Object getValue() {
        return value;
    }
    
    
    public void setValue(Object value) {
        this.value = value;
    }
}
