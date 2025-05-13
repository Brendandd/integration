package integration.core.domain.configuration;

import java.util.Date;

import integration.core.domain.BaseIntegrationDomain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Property associated with a single message flow.
 * 
 * @author Brendan Douglas
 */
@Entity
@Table(name = "component_property")
public class IntegrationComponentProperty extends BaseIntegrationDomain  {
    private String key;
    private String value;
    private IntegrationComponent component;
    private Date endDate;

    
    private IntegrationComponentProperty() {
        
    }
    
    public IntegrationComponentProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "component_id")
    public IntegrationComponent getComponent() {
        return component;
    }

    public void setComponent(IntegrationComponent component) {
        this.component = component;
    }

    
    @Column(name = "property_key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name = "value")
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }

    
    @Column(name = "end_date")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getEndDate() {
        return endDate;
    }
    
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
