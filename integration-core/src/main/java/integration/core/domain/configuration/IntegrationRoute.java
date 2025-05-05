package integration.core.domain.configuration;

import java.util.ArrayList;
import java.util.List;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * A route. A route is composed of components.
 * 
 * @author Brendan Douglas
 *
 */
@Entity
@Table(name = "route")
public class IntegrationRoute extends BaseIntegrationDomain {
    private String name;
    private String owner;

    private List<IntegrationComponent> components = new ArrayList<IntegrationComponent>();

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    public List<IntegrationComponent> getComponents() {
        return components;
    }

    public void setComponents(List<IntegrationComponent> components) {
        this.components = components;
    }
    
    @Column(name = "owner")
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
