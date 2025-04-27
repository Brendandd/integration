package integration.core.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * A single message
 * 
 * @author Brendan Douglas
 */
public class MessageDto extends BaseDto {
    private static final long serialVersionUID = -5868048360433222690L;

    private String content;
    private String contentType;
    private List<MessageMetaDataDto>metaData = new ArrayList<>();
    
    
    public String getContent() {
        return content;
    }

    
    public void setContent(String content) {
        this.content = content;
    }

    
    public List<MessageMetaDataDto> getMetaData() {
        return metaData;
    }

    
    public void setMetaData(List<MessageMetaDataDto> metaData) {
        this.metaData = metaData;
    }
    
    public void addMetaData(MessageMetaDataDto dto) {
        metaData.add(dto);
    }

    
    public String getContentType() {
        return contentType;
    }

    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
