package integration.core.service;

import java.util.List;

import integration.core.dto.ComponentDto;
import integration.core.dto.RouteDto;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.BaseRoute;
import integration.core.messaging.component.MessagingComponent;

/**
 * @author Brendan Douglas
 *
 */
public interface ConfigurationService {

    RouteDto getRouteByName(String name, String owner) throws ConfigurationException;

    List<RouteDto> getAllRoutes() throws ConfigurationException;

    void configureRoute(BaseRoute baseRoute, List<MessagingComponent> components);
    
    ComponentDto getComponent(long componentId);
}
