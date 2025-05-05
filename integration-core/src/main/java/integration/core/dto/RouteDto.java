package integration.core.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * A route.
 * 
 * @author Brendan Douglas
 */
public class RouteDto extends BaseDto {
    private static final long serialVersionUID = 7968596607654658242L;

    private String name;
    private String owner;

    private List<ComponentDto> components = new ArrayList<>();

    public String getName() {
        return name;
    }
    
    
    public void setName(String name) {
        this.name = name;
    }

    
    public List<ComponentDto> getComponents() {
        return components;
    }
    
    
    public void setComponents(List<ComponentDto> components) {
        this.components = components;
    }
    
    
    public void addComponent(ComponentDto component) {
        this.components.add(component);
    }


    public String getOwner() {
        return owner;
    }


    public void setOwner(String owner) {
        this.owner = owner;
    }
}
