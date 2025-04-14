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
    private String description;

    private List<ComponentRouteDto> components = new ArrayList<>();

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

    public List<ComponentRouteDto> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentRouteDto> components) {
        this.components = components;
    }

    public void addComponent(ComponentRouteDto component) {
        this.components.add(component);
    }

    public boolean hasComponent(String name) {
        for (ComponentRouteDto componentRoute : this.getComponents()) {
            if (componentRoute.getComponent().getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public ComponentDto getComponent(String name) {
        for (ComponentRouteDto componentRoute : this.getComponents()) {
            if (componentRoute.getComponent().getName().equals(name)) {
                return componentRoute.getComponent();
            }
        }

        return null;
    }
}
