package integration.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.configuration.ComponentRoute;
import integration.core.domain.configuration.ComponentState;
import integration.core.domain.configuration.Route;
import integration.core.dto.ComponentDto;
import integration.core.dto.ComponentRouteDto;
import integration.core.dto.RouteDto;
import integration.core.dto.mapper.ComponentMapper;
import integration.core.dto.mapper.ComponentRouteMapper;
import integration.core.dto.mapper.RouteMapper;
import integration.core.exception.ConfigurationException;
import integration.core.repository.ComponentRepository;
import integration.core.repository.ComponentRouteRepository;
import integration.core.repository.RouteRepository;
import integration.core.service.ConfigurationService;

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class ConfigurationServiceImpl implements ConfigurationService {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private ComponentRouteRepository componentRouteRepository;

    /**
     * Retrieves a route by name.
     */
    @Override
    public RouteDto getRouteByName(String name) throws ConfigurationException {
        Route route = routeRepository.getByName(name);

        if (route != null) {
            RouteMapper routeMapper = new RouteMapper();
            return routeMapper.doMapping(route);

        }

        // TODO map the components for the route.

        throw new ConfigurationException("Route not found.  Route name: " + name);
    }

    @Override
    public ComponentDto getComponentByName(String name) throws ConfigurationException {
        integration.core.domain.configuration.Component component = componentRepository.getByName(name);

        if (component != null) {
            ComponentMapper componentMapper = new ComponentMapper();
            return componentMapper.doMapping(component);
        }

        throw new ConfigurationException("Component not found.  Component name: " + name);
    }

    /**
     * Is this component running.
     */
    @Override
    public boolean isInboundRunning(long componentRouteId) throws ConfigurationException {
        ComponentRoute componentRoute = getComponentRoute(componentRouteId);

        return componentRoute.getInboundState() == ComponentState.RUNNING;
    }

    /**
     * Is this component stopped.
     */
    @Override
    public boolean isInboundStopped(long componentRouteId) throws ConfigurationException {
        ComponentRoute componentRoute = getComponentRoute(componentRouteId);

        return componentRoute.getInboundState() == ComponentState.STOPPED;
    }

    /**
     * Is this component running.
     */
    @Override
    public boolean isOutboundRunning(long componentRouteId) throws ConfigurationException {
        ComponentRoute componentRoute = getComponentRoute(componentRouteId);

        return componentRoute.getOutboundState() == ComponentState.RUNNING;
    }

    /**
     * Is this component stopped.
     */
    @Override
    public boolean isOutboundStopped(long componentRouteId) throws ConfigurationException {
        ComponentRoute componentRoute = getComponentRoute(componentRouteId);

        return componentRoute.getOutboundState() == ComponentState.STOPPED;
    }

    @Override
    public List<RouteDto> getAllRoutes() throws ConfigurationException {
        List<Route> routes = routeRepository.getAllRoutes();

        List<RouteDto> routeDtos = new ArrayList<RouteDto>();

        for (Route route : routes) {
            RouteMapper routeMapper = new RouteMapper();

            RouteDto routeDto = routeMapper.doMapping(route);
            routeDtos.add(routeDto);

            // TODO map the components for the route.
        }

        return routeDtos;
    }

    public ComponentRoute getComponentRoute(long componentRouteId) throws ConfigurationException {
        Optional<ComponentRoute> componentRouteOptional = componentRouteRepository.findById(componentRouteId);

        if (componentRouteOptional == null) {
            throw new ConfigurationException("Component does not exist for route.  ComponentRouteId: " + componentRouteId);
        }

        return componentRouteOptional.get();
    }

    public ComponentRoute getComponentForRoute(long componentId, long routeId) throws ConfigurationException {
        ComponentRoute componentRoute = componentRouteRepository.getByComponentAndRouteId(componentId, routeId);

        if (componentRoute == null) {
            throw new ConfigurationException(
                    "Component does not exist for route.  Component: " + componentId + ", routeId: " + routeId);
        }

        return componentRoute;
    }

    @Override
    public ComponentRouteDto getComponentRoute(long componentId, long routeId) throws ConfigurationException {
        ComponentRoute componentRoute = componentRouteRepository.getByComponentAndRouteId(componentId, routeId);

        if (componentRoute == null) {
            throw new ConfigurationException(
                    "Component does not exist for route.  Component: " + componentId + ", routeId: " + routeId);
        }

        ComponentRouteMapper mapper = new ComponentRouteMapper();

        return mapper.doMapping(componentRoute);
    }
}
