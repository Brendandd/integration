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
@Table(name = "message_flow_error")
public class MessageFlowError extends BaseIntegrationDomain {
    private MessageFlow messageFlow;
    private String details;

    @OneToOne
    @JoinColumn(name = "message_flow_id", nullable =  false)
    public MessageFlow getMessageFlow() {
        return messageFlow;
    }

    public void setMessageFlow(MessageFlow messageFlow) {
        this.messageFlow = messageFlow;
        messageFlow.setError(this);
    }

    @Column(name = "details")
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
