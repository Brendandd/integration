package integration.core.domain.configuration;

import java.util.ArrayList;
import java.util.List;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * A component which can be a communication point or a procesing step.
 * 
 * @author Brendan Douglas
 *
 */

@Entity
@Table(name = "component")
public class Component extends BaseIntegrationDomain {
    private String name;
    private String description;
    private ComponentType type;
    private ComponentCategory category;
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

    @ManyToOne
    @JoinColumn(name = "component_type_id", nullable = false)
    public ComponentType getType() {
        return type;
    }

    @ManyToOne
    @JoinColumn(name = "component_category_id", nullable = false)
    public ComponentCategory getCategory() {
        return category;
    }

    public void setCategory(ComponentCategory category) {
        this.category = category;
    }

    public void setType(ComponentType type) {
        this.type = type;
    }

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    public List<ComponentRoute> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentRoute> components) {
        this.components = components;
    }
}
