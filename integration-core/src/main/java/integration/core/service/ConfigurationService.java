package integration.core.service;

import java.util.List;

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

    boolean isInboundRunning(long componentId) throws ConfigurationException;

    boolean isInboundStopped(long componentId) throws ConfigurationException;

    boolean isOutboundRunning(long componentId) throws ConfigurationException;

    boolean isOutboundStopped(long componentId) throws ConfigurationException;

    void configureRoute(BaseRoute baseRoute, List<MessagingComponent> components);
}
