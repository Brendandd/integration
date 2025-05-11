package integration.core.service;

import java.util.List;

import integration.core.dto.ComponentDto;
import integration.core.dto.RouteDto;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.BaseRoute;
import integration.core.messaging.component.MessagingComponent;
import integration.core.service.impl.StatusChangeResponse;

/**
 * @author Brendan Douglas
 *
 */
public interface ConfigurationService {

    List<RouteDto> getAllRoutes() throws ConfigurationException;
    
    RouteDto getRoute(long routeId) throws ConfigurationException;

    void configureRoute(BaseRoute baseRoute, List<MessagingComponent> components) throws ConfigurationException;
    
    ComponentDto getComponent(long componentId) throws ConfigurationException;
    
    List<ComponentDto> getAllComponents() throws ConfigurationException;
    
    StatusChangeResponse stopComponentInbound(long id) throws ConfigurationException;
    
    StatusChangeResponse startComponentInbound(long id) throws ConfigurationException;  
    
    StatusChangeResponse stopComponentOutbound(long id) throws ConfigurationException;
    
    StatusChangeResponse startComponentOutbound(long id) throws ConfigurationException;
}


