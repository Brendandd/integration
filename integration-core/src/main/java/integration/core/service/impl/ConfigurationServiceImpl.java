package integration.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.configuration.ComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.configuration.IntegrationRoute;
import integration.core.dto.ComponentDto;
import integration.core.dto.RouteDto;
import integration.core.dto.mapper.ComponentMapper;
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
       
    @Autowired(required = false)
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

    
    @Override
    public List<RouteDto> getAllRoutes() throws ConfigurationException {
        List<IntegrationRoute> routes = routeRepository.getAllRoutes();

        List<RouteDto> routeDtos = new ArrayList<>();

        for (IntegrationRoute route : routes) {
            RouteMapper routeMapper = new RouteMapper();

            RouteDto routeDto = routeMapper.doMapping(route);
            routeDtos.add(routeDto);

            // TODO map the components for the route.
        }

        return routeDtos;
    }

    
    @Override
    public RouteDto getRoute(long routeId) throws ConfigurationException {
        Optional<IntegrationRoute> routeOptional = routeRepository.findById(routeId);

        if (routeOptional == null) {
            throw new ConfigurationException("Route does not exist: " + routeId);
        }
        
        RouteMapper mapper = new RouteMapper();
        
        return mapper.doMapping(routeOptional.get());
    }


    @Override
    public List<ComponentDto> getAllComponents() throws ConfigurationException {
        List<IntegrationComponent>components = componentRepository.getAllComponents();
        
        List<ComponentDto>componentDtos = new ArrayList<>();
        
        for (IntegrationComponent component : components) {
            ComponentMapper componentMapper = new ComponentMapper();
            
            ComponentDto componentDto = componentMapper.doMapping(component);
            componentDtos.add(componentDto);
        }
        return componentDtos;
    }

    
    @Override
    public ComponentDto getComponent(long componentId) throws ConfigurationException {
        Optional<IntegrationComponent> componentOptional = componentRepository.findById(componentId);

        if (componentOptional == null) {
            throw new ConfigurationException("Component does not exist: " + componentId);
        }
        
        ComponentMapper mapper = new ComponentMapper();
        
        return mapper.doMapping(componentOptional.get());
    }

    
    @Override
    public void configureRoute(BaseRoute integrationRoute, List<MessagingComponent> components) throws ConfigurationException{   
        String owner = env.getProperty("owner");       
        
        IntegrationRoute route = routeRepository.getByName(integrationRoute.getName(), owner);
              
        // Create a new route if the route doesn't exist in the current module.
        if (route == null) {
            route = new IntegrationRoute();
            route.setName(integrationRoute.getName());
            route.setCreatedUserId(owner);
            route.setOwner(owner);
            route = routeRepository.save(route);
        }
        
        // Set the route id
        integrationRoute.setIdentifier(route.getId());

        for (MessagingComponent component : components) {
            
            // If the component already exists then ignore and is the same type and category then it is probably just being configured part of another route which is fine.
            IntegrationComponent integrationComponent = componentRepository.getByNameAndRoute(component.getName(),integrationRoute.getName(), owner);
                           
            if (integrationComponent == null) { // Doesn't exist so create a new component
                integrationComponent = new IntegrationComponent();
                integrationComponent.setCategory(component.getCategory());
                integrationComponent.setName(component.getName());
                integrationComponent.setType(component.getType());
                integrationComponent.setCreatedUserId(owner);
                integrationComponent = componentRepository.save(integrationComponent);
                integrationComponent.setInboundState(ComponentStateEnum.RUNNING);
                integrationComponent.setOutboundState(ComponentStateEnum.RUNNING);
                integrationComponent.setOwner(owner);
                integrationComponent.setRoute(route);
            } 
            
            component.setIdentifier(integrationComponent.getId()); 
            component.setRoute(integrationRoute);
                   
            component.setInboundState(integrationComponent.getInboundState());
            component.setOutboundState(integrationComponent.getOutboundState());
            component.setConfiguration(configLoader.getConfiguration(route.getName(),component.getName()));
        }
    }

    
    @Override
    public StatusChangeResponse stopComponentInbound(long id) {
        Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
        IntegrationComponent component = componentOptional.get();
        
        if (component.getInboundState() == ComponentStateEnum.RUNNING) {
            component.setInboundState(ComponentStateEnum.STOPPED);
            componentRepository.save(component);
            
            return new StatusChangeResponse(true, "Inbound State Change", id, ComponentStateEnum.RUNNING, ComponentStateEnum.STOPPED);
        } 

        return new StatusChangeResponse(true, "Inbound already stopped", id, ComponentStateEnum.STOPPED, ComponentStateEnum.STOPPED);
    }

    
    @Override
    public StatusChangeResponse startComponentInbound(long id) {
        Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
        IntegrationComponent component = componentOptional.get();
        
        if (component.getInboundState() == ComponentStateEnum.STOPPED) {
            component.setInboundState(ComponentStateEnum.RUNNING);
            componentRepository.save(component);
            
            return new StatusChangeResponse(true, "Inbound State Change", id, ComponentStateEnum.STOPPED, ComponentStateEnum.RUNNING);
        } 
        
        return new StatusChangeResponse(true, "Inbound already started", id, ComponentStateEnum.RUNNING, ComponentStateEnum.RUNNING);
    }

    
    @Override
    public StatusChangeResponse stopComponentOutbound(long id) {
        Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
        IntegrationComponent component = componentOptional.get();
        
        if (component.getOutboundState() == ComponentStateEnum.RUNNING) {
            component.setOutboundState(ComponentStateEnum.STOPPED);
            componentRepository.save(component);
            
            return new StatusChangeResponse(true, "Outbound State Change", id, ComponentStateEnum.RUNNING, ComponentStateEnum.STOPPED);
        }  
        
        return new StatusChangeResponse(true, "Outbound already stopped", id, ComponentStateEnum.STOPPED, ComponentStateEnum.STOPPED);
    }

    
    @Override
    public StatusChangeResponse startComponentOutbound(long id) {
        Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
        IntegrationComponent component = componentOptional.get();
        
        if (component.getOutboundState() == ComponentStateEnum.STOPPED) {
            component.setOutboundState(ComponentStateEnum.RUNNING);
            componentRepository.save(component);
            
            return new StatusChangeResponse(true, "Outbound State Change", id, ComponentStateEnum.STOPPED, ComponentStateEnum.RUNNING);
        } 
        
        return new StatusChangeResponse(true, "Outbound already started", id, ComponentStateEnum.RUNNING, ComponentStateEnum.RUNNING);
    }
}
