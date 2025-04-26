package integration.core.domain.messaging;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Metadata associated with a group of message flow steps.  This can be anything but might include
 * the orginal filename for inbound directory adapters.
 * 
 * @author Brendan Douglas
 */
@Entity
@Table(name = "message_flow_group_metadata")
public class MessageFlowGroupMetaData extends BaseIntegrationDomain  {
    private String key;
    private String value;
    private MessageFlowGroup messageFlowGroup;
    
    private MessageFlowGroupMetaData() {
        
    }
    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "message_flow_group_id")
    public MessageFlowGroup getMessageFlowGroup() {
        return messageFlowGroup;
    }
    
    public void setMessageFlowGroup(MessageFlowGroup messageFlowGroup) {
        this.messageFlowGroup = messageFlowGroup;
    }
    
    public MessageFlowGroupMetaData(String key, String value) {
        this.key = key;
        this.value = value;
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
