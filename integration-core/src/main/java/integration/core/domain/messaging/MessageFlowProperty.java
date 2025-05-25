package integration.core.domain.messaging;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Property associated with a single message flow.
 * 
 * @author Brendan Douglas
 */
@Entity
@Table(name = "message_flow_property")
public class MessageFlowProperty extends BaseIntegrationDomain  {
    private String key;
    private String value;
    private MessageFlow messageFlow;
    
    private MessageFlowProperty() {
        
    }
    
    public MessageFlowProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "message_flow_id")
    public MessageFlow getMessageFlow() {
        return messageFlow;
    }
    
    public void setMessageFlow(MessageFlow messageFlow) {
        this.messageFlow = messageFlow;
    }

    @Column(name = "property_key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name = "value")
    public Object getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
