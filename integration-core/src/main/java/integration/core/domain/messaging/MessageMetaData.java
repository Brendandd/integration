package integration.core.domain.messaging;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Metadata associated with a single message.
 * 
 * @author Brendan Douglas
 */
@Entity
@Table(name = "message_metadata")
public class MessageMetaData extends BaseIntegrationDomain  {
    private String key;
    private String value;
    private Message message;
    
    private MessageMetaData() {
        
    }
    
    public MessageMetaData(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "message_id")
    public Message getMessage() {
        return message;
    }
    
    public void setMessage(Message message) {
        this.message= message;
    }

    @Column(name = "metadata_key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
