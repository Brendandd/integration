package integration.core.domain.messaging;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The status of a message flow.
 * 
 * @author Brendan Douglas
 */
@Entity
@Table(name = "message_flow_status")
public class MessageFlowStatus extends BaseIntegrationDomain {
    private MessageFlow messageFlow;
    private MessageFlowActionType statusType;

    
    public MessageFlowStatus(MessageFlowActionType statusType) {
        this.statusType = statusType;
    }
    
    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "message_flow_id")
    public MessageFlow getMessageFlow() {
        return messageFlow;
    }

    
    public void setMessageFlow(MessageFlow messageFlow) {
        this.messageFlow = messageFlow;
    }

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    public MessageFlowActionType getStatusType() {
        return statusType;
    }

    
    public void setStatusType(MessageFlowActionType statusType) {
        this.statusType = statusType;
    }
}
