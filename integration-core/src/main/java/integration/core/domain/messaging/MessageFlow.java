package integration.core.domain.messaging;

import java.util.ArrayList;
import java.util.List;

import integration.core.domain.BaseIntegrationDomain;
import integration.core.domain.configuration.IntegrationComponent;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * A single flow of a message through the internal components.
 * 
 * @author Brendan Douglas
 * 
 */
@Entity
@Table(name = "message_flow")
public class MessageFlow extends BaseIntegrationDomain {
    private IntegrationComponent component;
    private Message message;
    private MessageFlow parentMessageFlow;
    private MessageFlowActionType action;
    
    private List<MessageFlowProperty>properties = new ArrayList<>();

    private MessageFlowGroup group;

    private MessageFlowFiltered filtered;
    private MessageFlowError error;

    public MessageFlow() {

    }

    @ManyToOne
    @JoinColumn(name = "component_id")
    public IntegrationComponent getComponent() {
        return component;
    }

    public void setComponent(IntegrationComponent component) {
        this.component = component;
    }

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @ManyToOne
    @JoinColumn(name = "parent_message_flow_id")
    public MessageFlow getParentMessageFlow() {
        return parentMessageFlow;
    }

    public void setParentMessageFlow(MessageFlow parentMessageFlow) {
        this.parentMessageFlow = parentMessageFlow;
    }

    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id")
    public MessageFlowGroup getGroup() {
        return group;
    }

    public void setGroup(MessageFlowGroup group) {
        this.group = group;
    }

    @OneToOne(mappedBy = "messageFlow", optional = true, cascade = CascadeType.ALL)
    public MessageFlowFiltered getFiltered() {
        return filtered;
    }

    public void setFiltered(MessageFlowFiltered filtered) {
        this.filtered = filtered;
    }

    
    @OneToOne(mappedBy = "messageFlow", optional = true, cascade = CascadeType.ALL)
    public MessageFlowError getError() {
        return error;
    }

    
    public void setError(MessageFlowError error) {
        this.error = error;
    }

    
    @Column(name="action")
    @Enumerated(EnumType.STRING)
    public MessageFlowActionType getAction() {
        return action;
    }

    public void setAction(MessageFlowActionType action) {
        this.action = action;
    }
    
    
    @OneToMany(mappedBy = "messageFlow", cascade = CascadeType.ALL)
    public List<MessageFlowProperty> getProperties() {
        return properties;
    }

    
    public void setProperties(List<MessageFlowProperty> properties) {
        this.properties = properties;
    }

    
    public boolean containsProperty(String key) {
        for (MessageFlowProperty property : properties) {
            if (property.getKey().equals(key)) {
                return true;
            }
        }
        
        return false;
    }

    
    @Transient
    public MessageFlowProperty getProperty(String key) {
        for (MessageFlowProperty property : properties) {
            if (property.getKey().equals(key)) {
                return property;
            }
        }
        
        return null;
    }

    
    public void addOrUpdateProperty(String key, Object value) {
        MessageFlowProperty property = getProperty(key);
        
        if (property == null) {
            property = new MessageFlowProperty(key, String.valueOf(value));
            property.setMessageFlow(this);
        } else {
            property.setValue(String.valueOf(value));
        }
        
        this.properties.add(property);
    }
}
