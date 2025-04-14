package integration.core.domain.messaging;

import java.util.ArrayList;
import java.util.List;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * A grouping of message flow steps. Used to determine what steps are related to
 * the original incoming message.
 * 
 * @author Brendan Douglas
 */
@Entity
@Table(name = "message_flow_group")
public class MessageFlowGroup extends BaseIntegrationDomain {
    private List<MessageFlowStep> messageFlowSteps = new ArrayList<>();

    @OneToMany(mappedBy = "messageFlowGroup", cascade = CascadeType.ALL)
    public List<MessageFlowStep> getMessageFlowSteps() {
        return messageFlowSteps;
    }

    public void setMessageFlowSteps(List<MessageFlowStep> messageFlowSteps) {
        this.messageFlowSteps = messageFlowSteps;
    }

    /**
     * Adds a step to this message flow.
     * 
     * @param messageFlowStep
     */
    public void addMessageFlowStep(MessageFlowStep messageFlowStep) {
        this.messageFlowSteps.add(messageFlowStep);

        messageFlowStep.setMessageFlowGroup(this);
    }
}
