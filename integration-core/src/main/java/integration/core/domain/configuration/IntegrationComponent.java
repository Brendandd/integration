package integration.core.domain.configuration;

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
 * A component which can be an adapter point or a procesing step (filter, splitter, transformer etc).
 * 
 * @author Brendan Douglas
 *
 */

@Entity
@Table(name = "component")
public class IntegrationComponent extends BaseIntegrationDomain {
    private String name;
    private String owner;
    private IntegrationRoute route;
    
    private ComponentState inboundState;

    private ComponentState outboundState;
    
    private ComponentType type;
    private ComponentCategory category;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public ComponentType getType() {
        return type;
    }

    public void setType(ComponentType type) {
        this.type = type;
    }
    
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    public ComponentCategory getCategory() {
        return category;
    }
    
    public void setCategory(ComponentCategory category) {
        this.category = category;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    @Column(name = "owner")
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "route_id")
    public IntegrationRoute getRoute() {
        return route;
    }

    public void setRoute(IntegrationRoute route) {
        this.route = route;
    }
    
    @Column(name = "inbound_state")
    @Enumerated(EnumType.STRING)
    public ComponentState getInboundState() {
        return inboundState;
    }
    
    
    public void setInboundState(ComponentState inboundState) {
        this.inboundState = inboundState;
    }
    
    
    @Column(name = "outbound_state")
    @Enumerated(EnumType.STRING)
    public ComponentState getOutboundState() {
        return outboundState;
    }

    
    public void setOutboundState(ComponentState outboundState) {
        this.outboundState = outboundState;
    }
}
