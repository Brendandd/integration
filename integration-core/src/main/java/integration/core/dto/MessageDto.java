package integration.core.dto;

import integration.core.domain.configuration.ContentTypeEnum;

/**
 * A single message
 * 
 * @author Brendan Douglas
 */
public class MessageDto extends BaseDto {
    private static final long serialVersionUID = -5868048360433222690L;

    private String content;
    private ContentTypeEnum contentType;
    
    
    public String getContent() {
        return content;
    }

    
    public void setContent(String content) {
        this.content = content;
    }

    
    public ContentTypeEnum getContentType() {
        return contentType;
    }

    
    public void setContentType(ContentTypeEnum contentType) {
        this.contentType = contentType;
    }
}
