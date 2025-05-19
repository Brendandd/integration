package integration.core.domain.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.Where;

import integration.core.domain.BaseIntegrationDomain;
import integration.core.domain.messaging.MessageFlowEvent;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
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
    
    private IntegrationComponentStateEnum inboundState;
    private IntegrationComponentStateEnum outboundState;
    
    private IntegrationComponentTypeEnum type;
    private IntegrationComponentCategoryEnum category;
    
    
    private List<MessageFlowEvent>events = new ArrayList<>();
    private Map<String, IntegrationComponentProperty> properties = new HashMap<>();
    

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public IntegrationComponentTypeEnum getType() {
        return type;
    }

    public void setType(IntegrationComponentTypeEnum type) {
        this.type = type;
    }
    
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    public IntegrationComponentCategoryEnum getCategory() {
        return category;
    }
    
    public void setCategory(IntegrationComponentCategoryEnum category) {
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
    public IntegrationComponentStateEnum getInboundState() {
        return inboundState;
    }
    
    
    public void setInboundState(IntegrationComponentStateEnum inboundState) {
        this.inboundState = inboundState;
    }
    
    
    @Column(name = "outbound_state")
    @Enumerated(EnumType.STRING)
    public IntegrationComponentStateEnum getOutboundState() {
        return outboundState;
    }

    
    public void setOutboundState(IntegrationComponentStateEnum outboundState) {
        this.outboundState = outboundState;
    }

    
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    public List<MessageFlowEvent> getEvents() {
        return events;
    }

    
    public void setEvents(List<MessageFlowEvent> events) {
        this.events = events;
    }

    
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    @MapKey(name = "key")
    @Where(clause = "end_date IS NULL")
    public Map<String, IntegrationComponentProperty> getProperties() {
        return properties;
    }
    
    
    public void setProperties(Map<String, IntegrationComponentProperty> properties) {
        this.properties = properties;
    }   

    
    /**
     * Adds a new property to this component.
     * 
     * @param key
     * @param value
     */
    public void addProperty(String key, String value) {
        IntegrationComponentProperty property = new IntegrationComponentProperty(key, value);
        property.setComponent(this);
        
        this.getProperties().put(key, property);
    }
}
