package integration.core.dto;

public class ComponentPropertyDto extends BaseDto {
    private static final long serialVersionUID = -3299101722640187929L;
    
    private String key;
    private String value;

    public String getKey() {
        return key;
    }
    
    
    public void setKey(String key) {
        this.key = key;
    }
    
    
    public String getValue() {
        return value;
    }
    
    
    public void setValue(String value) {
        this.value = value;
    }
}
