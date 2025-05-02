package integration.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.configuration.ComponentState;
import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.configuration.IntegrationRoute;
import integration.core.dto.RouteDto;
import integration.core.dto.mapper.RouteMapper;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.BaseRoute;
import integration.core.messaging.component.MessagingComponent;
import integration.core.messaging.component.RouteConfigLoader;
import integration.core.repository.ComponentRepository;
import integration.core.repository.RouteRepository;
import integration.core.service.ConfigurationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class ConfigurationServiceImpl implements ConfigurationService {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ComponentRepository componentRepository;
       
    @Autowired
    private RouteConfigLoader configLoader;
    
    @Autowired
    private Environment env;

    /**
     * Retrieves a route by name.
     */
    @Override
    public RouteDto getRouteByName(String name, String owner) throws ConfigurationException {
        IntegrationRoute route = routeRepository.getByName(name, owner);

        if (route != null) {
            RouteMapper routeMapper = new RouteMapper();
            return routeMapper.doMapping(route);

        }

        throw new ConfigurationException("Route not found.  Route name: " + name);
    }

    
    /**
     * Is this component running.
     */
    @Override
    public boolean isInboundRunning(long componentId) throws ConfigurationException {
        IntegrationComponent component = getComponent(componentId);

        return component.getInboundState() == ComponentState.RUNNING;
    }
    
    
    /**
     * Is this component stopped.
     */
    @Override
    public boolean isInboundStopped(long componentId) throws ConfigurationException {
        IntegrationComponent component = getComponent(componentId);

        return component.getInboundState() == ComponentState.STOPPED;
    }
    
    
    /**
     * Is this component running.
     */
    @Override
    public boolean isOutboundRunning(long componentId) throws ConfigurationException {
        IntegrationComponent component = getComponent(componentId);

        return component.getOutboundState() == ComponentState.RUNNING;
    }

    /**
     * Is this component stopped.
     */
    @Override
    public boolean isOutboundStopped(long componentId) throws ConfigurationException {
        IntegrationComponent component = getComponent(componentId);

        return component.getOutboundState() == ComponentState.STOPPED;
    }

    @Override
    public List<RouteDto> getAllRoutes() throws ConfigurationException {
        List<IntegrationRoute> routes = routeRepository.getAllRoutes();

        List<RouteDto> routeDtos = new ArrayList<RouteDto>();

        for (IntegrationRoute route : routes) {
            RouteMapper routeMapper = new RouteMapper();

            RouteDto routeDto = routeMapper.doMapping(route);
            routeDtos.add(routeDto);

            // TODO map the components for the route.
        }

        return routeDtos;
    }
    
    
    public IntegrationComponent getComponent(long componentId) throws ConfigurationException {
        Optional<IntegrationComponent> componentOptional = componentRepository.findById(componentId);

        if (componentOptional == null) {
            throw new ConfigurationException("Component does not exist: " + componentId);
        }
        
        return componentOptional.get();
    }

    
    @Override
    public void configureRoute(BaseRoute baseRoute, List<MessagingComponent> components) {   
        String owner = env.getProperty("owner");       
        
        IntegrationRoute route = routeRepository.getByName(baseRoute.getName(), owner);
              
        // Create a new route if the route doesn't exist in the current module.
        if (route == null) {
            route = new IntegrationRoute();
            route.setName(baseRoute.getName());
            route.setCreatedUserId(owner);
            route.setOwner(owner);
            route = routeRepository.save(route);
        }
        
        // Set the route id
        baseRoute.setIdentifier(route.getId());

        for (MessagingComponent component : components) {
            
            // If the component already exists then ignore and is the same type and category then it is probably just being configured part of another route which is fine.
            integration.core.domain.configuration.IntegrationComponent c = componentRepository.getByNameAndRoute(component.getName(),baseRoute.getName(), owner);
                           
            if (c == null) { // Doesn't exist so create a new component
                c = new integration.core.domain.configuration.IntegrationComponent();
                c.setCategory(component.getCategory());
                c.setName(component.getName());
                c.setType(component.getType());
                c.setCreatedUserId(owner);
                c = componentRepository.save(c);
                c.setInboundState(ComponentState.RUNNING);
                c.setOutboundState(ComponentState.RUNNING);
                c.setOwner(owner);
                c.setRoute(route);
            } 
            
            component.setIdentifier(c.getId()); 
            component.setRoute(baseRoute);
                   
            component.setInboundRunning(c.getInboundState() == ComponentState.RUNNING);
            component.setOutboundRunning(c.getOutboundState() == ComponentState.RUNNING);
            component.setConfiguration(configLoader.getConfiguration(route.getName(),component.getName()));
        }
    }
}
