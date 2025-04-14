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
public class Route extends BaseIntegrationDomain {
    private String name;
    private String description;

    private List<ComponentRoute> components = new ArrayList<ComponentRoute>();

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    public List<ComponentRoute> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentRoute> components) {
        this.components = components;
    }
}
