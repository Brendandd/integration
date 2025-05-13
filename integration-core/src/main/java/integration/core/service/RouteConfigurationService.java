package integration.core.service;

import java.util.List;

import integration.core.dto.RouteDto;
import integration.core.exception.ConfigurationException;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.component.MessagingComponent;

/**
 * @author Brendan Douglas
 *
 */
public interface RouteConfigurationService {
    List<RouteDto> getAllRoutes() throws ConfigurationException;
    
    RouteDto getRoute(long routeId) throws ConfigurationException;

    void configureRoute(BaseRoute baseRoute, List<MessagingComponent> components) throws ConfigurationException;
}


