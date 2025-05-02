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
 * The status of a message flow step.
 * 
 * @author Brendan Douglas
 */
@Entity
@Table(name = "message_flow_status")
public class MessageFlowStatus extends BaseIntegrationDomain {
    private MessageFlowStep messageFlowStep;
    private MessageFlowStepActionType statusType;

    
    public MessageFlowStatus(MessageFlowStepActionType statusType) {
        this.statusType = statusType;
    }
    
    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "message_flow_step_id")
    public MessageFlowStep getMessageFlowStep() {
        return messageFlowStep;
    }

    
    public void setMessageFlowStep(MessageFlowStep messageFlowStep) {
        this.messageFlowStep = messageFlowStep;
    }

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    public MessageFlowStepActionType getStatusType() {
        return statusType;
    }

    
    public void setStatusType(MessageFlowStepActionType statusType) {
        this.statusType = statusType;
    }
}
