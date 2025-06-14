package integration.core.domain.messaging;

import java.util.Date;

import integration.core.domain.BaseIntegrationDomain;
import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.configuration.IntegrationRoute;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "outbox_event")
public class OutboxEvent extends BaseIntegrationDomain {
    private MessageFlow messageFlow;
    private IntegrationComponent component;
    private IntegrationRoute route;
    private Date retryAfter;
    private int retryCount;
    private String owner;
    private String error;

    @ManyToOne
    @JoinColumn(name = "message_flow_id")
    public MessageFlow getMessageFlow() {
        return messageFlow;
    }
    
    
    public void setMessageFlow(MessageFlow messageFlow) {
        this.messageFlow = messageFlow;
    }

    
    @ManyToOne
    @JoinColumn(name = "route_id")
    public IntegrationRoute getRoute() {
        return route;
    }
    
    
    public void setRoute(IntegrationRoute route) {
        this.route = route;
    }
    
    
    @ManyToOne
    @JoinColumn(name = "component_id")
    public IntegrationComponent getComponent() {
        return component;
    }
    
    
    public void setComponent(IntegrationComponent component) {
        this.component = component;
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


    @Column(name = "owner")
    public String getOwner() {
        return owner;
    }


    public void setOwner(String owner) {
        this.owner = owner;
    }



    @Column(name = "error")
    public String getError() {
        return error;
    }


    public void setError(String error) {
        this.error = error;
    } 
}
