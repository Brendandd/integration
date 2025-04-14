package integration.core.service;

import java.util.List;

import integration.core.dto.ComponentDto;
import integration.core.dto.ComponentRouteDto;
import integration.core.dto.RouteDto;
import integration.core.exception.ConfigurationException;

/**
 * @author Brendan Douglas
 *
 */
public interface ConfigurationService {

    /**
     * Retrieve a route by name.
     * 
     * @param name
     * @return
     * @throws ConfigurationException
     */
    RouteDto getRouteByName(String name) throws ConfigurationException;

    /**
     * Retrieves all the routes.
     * 
     * @return
     * @throws ConfigurationException
     */
    List<RouteDto> getAllRoutes() throws ConfigurationException;

    boolean isInboundRunning(long componentRouteId) throws ConfigurationException;

    boolean isInboundStopped(long componentRouteId) throws ConfigurationException;

    boolean isOutboundRunning(long componentRouteId) throws ConfigurationException;

    boolean isOutboundStopped(long componentRouteId) throws ConfigurationException;

    /**
     * Gets a component by name.
     * 
     * @param componentName
     * @return
     */
    ComponentDto getComponentByName(String componentName);

    /**
     * Gets a component route by route and component id.
     * 
     * @param componentId
     * @param routeId
     * @return
     * @throws ConfigurationException
     */
    ComponentRouteDto getComponentRoute(long componentId, long routeId) throws ConfigurationException;
}
