package integration.core.domain.messaging;

import integration.core.domain.BaseIntegrationDomain;
import integration.core.domain.configuration.IntegrationComponent;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * A single flow of a message through the internal components.
 * 
 * @author Brendan Douglas
 * 
 */
@Entity
@Table(name = "message_flow_step")
public class MessageFlowStep extends BaseIntegrationDomain {
    private IntegrationComponent component;
    private Message message;
    private MessageFlowStep fromMessageFlowStep;
    private MessageFlowStepActionType action;

    private MessageFlowGroup messageFlowGroup;

    private MessageFlowStepFiltered filteredStep;

    public MessageFlowStep() {

    }

    @ManyToOne
    @JoinColumn(name = "component_id")
    public IntegrationComponent getComponent() {
        return component;
    }

    public void setComponent(IntegrationComponent component) {
        this.component = component;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "message_id")
    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @ManyToOne
    @JoinColumn(name = "from_message_flow_step_id")
    public MessageFlowStep getFromMessageFlowStep() {
        return fromMessageFlowStep;
    }

    public void setFromMessageFlowStep(MessageFlowStep fromMessageFlowStep) {
        this.fromMessageFlowStep = fromMessageFlowStep;
    }

    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "message_flow_group_id")
    public MessageFlowGroup getMessageFlowGroup() {
        return messageFlowGroup;
    }

    public void setMessageFlowGroup(MessageFlowGroup messageFlowGroup) {
        this.messageFlowGroup = messageFlowGroup;
    }

    @OneToOne(mappedBy = "messageFlowStep", optional = true, cascade = CascadeType.ALL)
    public MessageFlowStepFiltered getFilteredStep() {
        return filteredStep;
    }

    public void setFilteredStep(MessageFlowStepFiltered filteredStep) {
        this.filteredStep = filteredStep;
    }
    
    
    @Column(name="action")
    @Enumerated(EnumType.STRING)
    public MessageFlowStepActionType getAction() {
        return action;
    }

    public void setAction(MessageFlowStepActionType action) {
        this.action = action;
    }
}
