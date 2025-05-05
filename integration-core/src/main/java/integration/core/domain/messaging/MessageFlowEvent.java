package integration.core.domain.messaging;

import java.util.Date;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

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
    private MessageFlowEventType type;
    private String componentPath;
    private String owner;
    private Date retryAfter;
    private int retryCount;

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
    public MessageFlowEventType getType() {
        return type;
    }

    public void setType(MessageFlowEventType type) {
        this.type = type;
    }

    @Column(name = "owner")
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Column(name = "component_path")
    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    @Column(name = "retry_after")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getRetryAfter() {
        return retryAfter;
    }
    
    public void setRetryAfter(Date retryAfter) {
        this.retryAfter = retryAfter;
    }

    @Column(name = "retry_count")
    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    } 
}
