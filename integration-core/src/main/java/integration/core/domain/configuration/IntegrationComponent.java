package integration.core.domain.configuration;

import java.util.ArrayList;
import java.util.List;

import integration.core.domain.BaseIntegrationDomain;
import integration.core.domain.messaging.MessageFlowEvent;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    
    private ComponentStateEnum inboundState;
    private ComponentStateEnum outboundState;
    
    private ComponentTypeEnum type;
    private ComponentCategoryEnum category;
    
    private List<MessageFlowEvent>events = new ArrayList<>();

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public ComponentTypeEnum getType() {
        return type;
    }

    public void setType(ComponentTypeEnum type) {
        this.type = type;
    }
    
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    public ComponentCategoryEnum getCategory() {
        return category;
    }
    
    public void setCategory(ComponentCategoryEnum category) {
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
    public ComponentStateEnum getInboundState() {
        return inboundState;
    }
    
    
    public void setInboundState(ComponentStateEnum inboundState) {
        this.inboundState = inboundState;
    }
    
    
    @Column(name = "outbound_state")
    @Enumerated(EnumType.STRING)
    public ComponentStateEnum getOutboundState() {
        return outboundState;
    }

    
    public void setOutboundState(ComponentStateEnum outboundState) {
        this.outboundState = outboundState;
    }

    
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    public List<MessageFlowEvent> getEvents() {
        return events;
    }
    
    
    public void setEvents(List<MessageFlowEvent> events) {
        this.events = events;
    }
}
