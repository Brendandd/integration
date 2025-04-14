package integration.core.domain.configuration;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * The system a message is being received from or being sent to.
 * 
 * @author Brendan Douglas
 */
@Entity
@Table(name = "external_system")
public class ExternalSystem extends BaseIntegrationDomain {
    private String name;
    private String description;

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
}
