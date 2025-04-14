package integration.core.dto;

/**
 * 
 * @author Brendan Douglas
 */
public class ComponentRouteDto extends BaseDto {
    private static final long serialVersionUID = -8804997083755626075L;

    private RouteDto route;
    private ComponentDto component;

    public RouteDto getRoute() {
        return route;
    }

    public ComponentDto getComponent() {
        return component;
    }

    public void setRoute(RouteDto route) {
        this.route = route;
    }

    public void setComponent(ComponentDto component) {
        this.component = component;
    }
}
