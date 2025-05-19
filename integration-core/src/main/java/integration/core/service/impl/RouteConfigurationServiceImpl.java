package integration.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.configuration.IntegrationComponentCategoryEnum;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationRoute;
import integration.core.dto.RouteDto;
import integration.core.dto.mapper.RouteMapper;
import integration.core.exception.ConfigurationException;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.exception.ResourceNotFoundException;
import integration.core.repository.ComponentRepository;
import integration.core.repository.RouteRepository;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.component.MessagingComponent;
import integration.core.runtime.messaging.component.RouteConfigLoader;
import integration.core.service.RouteConfigurationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class RouteConfigurationServiceImpl implements RouteConfigurationService {
    
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

    
    @Override
    public List<RouteDto> getAllRoutes() throws ConfigurationException {
        try {
            List<IntegrationRoute> routes = routeRepository.getAllRoutes();
    
            List<RouteDto> routeDtos = new ArrayList<>();
    
            for (IntegrationRoute route : routes) {
                RouteMapper routeMapper = new RouteMapper();
    
                RouteDto routeDto = routeMapper.doMapping(route);
                routeDtos.add(routeDto);
            }
            
            return routeDtos;
        } catch(DataAccessException e) {
            throw new ConfigurationException("Database error while getting all routes", new ArrayList<>(), e);
        }
    }

    
    @Override
    public RouteDto getRoute(long routeId) throws ConfigurationException {
        try {
            Optional<IntegrationRoute> routeOptional = routeRepository.findById(routeId);
    
            if (routeOptional.isEmpty()) {
                List<ExceptionIdentifier>identifiers = new ArrayList<>();
                identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.ROUTE_ID, routeId));
                throw new ResourceNotFoundException("Route does not exist", identifiers, false);
            }
            
            RouteMapper mapper = new RouteMapper();
            
            return mapper.doMapping(routeOptional.get());
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.ROUTE_ID, routeId));
            throw new ConfigurationException("Database error while getting a route by id", identifiers, e);
        }
    }

    
    @Override
    public void configureRoute(BaseRoute integrationRoute, List<MessagingComponent> components) throws ConfigurationException{   
        try {
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
                
                // See if the component already exists for the route and module.
                IntegrationComponent integrationComponent = componentRepository.getByNameAndRoute(component.getName(),integrationRoute.getName(), owner);
                               
                if (integrationComponent == null) { // Doesn't exist so create a new component
                    integrationComponent = new IntegrationComponent();
                    integrationComponent.setCategory(component.getCategory());
                    integrationComponent.setName(component.getName());
                    integrationComponent.setType(component.getType());
                    integrationComponent.setCreatedUserId(owner);
                    integrationComponent = componentRepository.save(integrationComponent);
                    integrationComponent.setInboundState(IntegrationComponentStateEnum.RUNNING);
                    integrationComponent.setOutboundState(IntegrationComponentStateEnum.RUNNING);
                    integrationComponent.setOwner(owner);
                    integrationComponent.setRoute(route);
                } 
                
                component.setIdentifier(integrationComponent.getId()); 
                component.setRoute(integrationRoute);
                       
                component.setInboundState(integrationComponent.getInboundState());
                component.setOutboundState(integrationComponent.getOutboundState());
                
                Map<String,String>configProperties = configLoader.getConfiguration(route.getName(),component.getName());
                component.setConfiguration(configProperties);
                
                for (Map.Entry<String, String> entry : configProperties.entrySet()) {
                    if (integrationComponent.getCategory() == IntegrationComponentCategoryEnum.OUTBOUND_ADAPTER || integrationComponent.getCategory() == IntegrationComponentCategoryEnum.INBOUND_ADAPTER) {
                        integrationComponent.addProperty(entry.getKey(), entry.getValue());
                    }
                }

                component.validateAnnotations();
            }
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.ROUTE_ID, integrationRoute.getIdentifier()));
            throw new ConfigurationException("Database error while configuring a component route association", identifiers, e);
        }
    }
}
