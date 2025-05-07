package integration.core.domain.messaging;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    private String contentType;
       
    private Message() {
        
    }

    public Message(String content, String contentType) {
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
    public String getContentType() {
        return contentType;
    }

    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
