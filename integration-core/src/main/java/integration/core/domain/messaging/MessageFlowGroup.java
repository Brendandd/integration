package integration.core.domain.messaging;

import java.util.ArrayList;
import java.util.List;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * A grouping of message flows. Used to determine what flows are related to
 * the original incoming message.
 * 
 * @author Brendan Douglas
 */
@Entity
@Table(name = "message_flow_group")
public class MessageFlowGroup extends BaseIntegrationDomain {
    private List<MessageFlow> messageFlows = new ArrayList<>();

    @OneToMany(mappedBy = "messageFlowGroup", cascade = CascadeType.ALL)
    public List<MessageFlow> getMessageFlows() {
        return messageFlows;
    }

    public void setMessageFlows(List<MessageFlow> messageFlows) {
        this.messageFlows = messageFlows;
    }

    /**
     * Adds a message flow to this group.
     * 
     * @param messageFlow
     */
    public void addMessageFlow(MessageFlow messageFlow) {
        this.messageFlows.add(messageFlow);

        messageFlow.setMessageFlowGroup(this);
    }
}
