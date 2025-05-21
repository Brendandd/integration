package integration.core.service.impl;

import java.util.List;
import java.util.Map;

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
import integration.core.repository.ComponentRepository;
import integration.core.repository.RouteRepository;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.component.MessagingComponent;
import integration.core.runtime.messaging.component.RouteConfigLoader;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import integration.core.service.StartupService;

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class StartupServiceImpl implements StartupService {
    
    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ComponentRepository componentRepository;
       
    @Autowired(required = false)
    private RouteConfigLoader configLoader;
    
    @Autowired
    private Environment env;

    
    @Override
    public void configureRoute(BaseRoute integrationRoute, List<MessagingComponent> components) throws RouteConfigurationException, ComponentConfigurationException{   
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
                Map<String,String>configProperties = configLoader.getConfiguration(route.getName(),component.getName());
                component.setConfiguration(configProperties);
                
                // See if the component already exists for the route.  Each component can only exist once.
                IntegrationComponent integrationComponent = componentRepository.getByNameAndRoute(component.getName(),route.getId());
                               
                if (integrationComponent == null) { // Doesn't exist so create a new component
                    integrationComponent = new IntegrationComponent();
                    integrationComponent.setCategory(component.getCategory());
                    integrationComponent.setName(component.getName());
                    integrationComponent.setType(component.getType());
                    integrationComponent.setCreatedUserId(owner);
                    integrationComponent = componentRepository.save(integrationComponent);
                    integrationComponent.setInboundState(IntegrationComponentStateEnum.RUNNING);
                    integrationComponent.setOutboundState(IntegrationComponentStateEnum.RUNNING);
                    integrationComponent.setRoute(route);
                    
                    // New component so save properties in the database
                    for (Map.Entry<String, String> entry : configProperties.entrySet()) {
                        if (integrationComponent.getCategory() == IntegrationComponentCategoryEnum.OUTBOUND_ADAPTER || integrationComponent.getCategory() == IntegrationComponentCategoryEnum.INBOUND_ADAPTER) {
                            
                            if (integrationComponent.getProperty(entry.getKey()) == null) {
                                integrationComponent.addProperty(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                    
                    componentRepository.save(integrationComponent);
                } 

                component.setIdentifier(integrationComponent.getId()); 
                component.setRoute(integrationRoute);
                       
                component.setInboundState(integrationComponent.getInboundState());
                component.setOutboundState(integrationComponent.getOutboundState());

                component.validateAnnotations();
            }
        } catch(DataAccessException e) {
            throw new RouteConfigurationException("Database error while configuring a component route association", integrationRoute.getIdentifier(), e);
        }
    }
}
