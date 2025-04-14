package integration.core.domain.messaging;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 
 * An event for a message. Processed by the transactional outbox.
 * 
 * @author Brendan Douglas
 *
 */
@Entity
@Table(name = "message_flow_event")
public class MessageFlowEvent extends BaseIntegrationDomain {
    private MessageFlowStep messageFlow;
    private MessageFlowTypeEvent type;

    @ManyToOne
    @JoinColumn(name = "message_flow_id")
    public MessageFlowStep getMessageFlow() {
        return messageFlow;
    }

    public void setMessageFlow(MessageFlowStep messageFlow) {
        this.messageFlow = messageFlow;
    }

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public MessageFlowTypeEvent getType() {
        return type;
    }

    public void setType(MessageFlowTypeEvent type) {
        this.type = type;
    }
}
