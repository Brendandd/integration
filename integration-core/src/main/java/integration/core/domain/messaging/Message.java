package integration.core.domain.messaging;

import integration.core.domain.BaseIntegrationDomain;
import integration.core.domain.configuration.ContentTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * A message.
 * 
 * @author Brendan Douglas
 *
 */
@Entity
@Table(name = "message")
public class Message extends BaseIntegrationDomain {
    private String content;
    private ContentTypeEnum contentType;
       
    public Message() {
        
    }

    public Message(String content, ContentTypeEnum contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    
    @Override
    public String toString() {
        return content;
    }

    @Column(name = "content_type")
    @Enumerated(EnumType.STRING)
    public ContentTypeEnum getContentType() {
        return contentType;
    }

    
    public void setContentType(ContentTypeEnum contentType) {
        this.contentType = contentType;
    }
}
