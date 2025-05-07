package integration.core.dto;

/**
 * A single message
 * 
 * @author Brendan Douglas
 */
public class MessageDto extends BaseDto {
    private static final long serialVersionUID = -5868048360433222690L;

    private String content;
    private String contentType;
    
    
    public String getContent() {
        return content;
    }

    
    public void setContent(String content) {
        this.content = content;
    }

    
    public String getContentType() {
        return contentType;
    }

    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
