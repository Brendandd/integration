package integration.core.domain.messaging;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Filtered messages.
 * 
 * @author Brendan Douglas
 */
@Entity
@Table(name = "message_flow_filtered")
public class MessageFlowFiltered extends BaseIntegrationDomain {
    private MessageFlow messageFlow;
    private String reason;
    private String name;

    @OneToOne
    @JoinColumn(name = "message_flow_id", nullable =  false)
    public MessageFlow getMessageFlow() {
        return messageFlow;
    }

    public void setMessageFlow(MessageFlow messageFlow) {
        this.messageFlow = messageFlow;
        messageFlow.setFilteredStep(this);
    }

    @Column(name = "reason")
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
