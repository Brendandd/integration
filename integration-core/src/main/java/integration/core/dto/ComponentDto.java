package integration.core.dto;

import java.util.HashMap;
import java.util.Map;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentType;

/**
 * 
 * A single component. A component is a communication point or a processing
 * step.
 * 
 * @author Brendan Douglas
 */
public class ComponentDto extends BaseDto {
    private static final long serialVersionUID = 1099848325612162806L;

    private String name;
    private String description;
    private ComponentType type;
    private ComponentCategory category;

    private Map<String, String> properties = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComponentType getType() {
        return type;
    }

    public void setType(ComponentType type) {
        this.type = type;
    }

    public ComponentCategory getCategory() {
        return category;
    }

    public void setCategory(ComponentCategory category) {
        this.category = category;
    }

    public void addComponentProperty(String key, String value) {
        this.properties.put(key, value);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
