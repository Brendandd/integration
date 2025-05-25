package integration.core.service;

import java.util.List;

import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.component.MessagingComponent;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;

/**
 * A service to configure a route and components at startup.
 * 
 * @author Brendan Douglas
 *
 */
public interface StartupService {
    
    /**
     * Configures a route and its components at startup time.
     * 
     * @param baseRoute
     * @param components
     * @throws RouteConfigurationException
     * @throws ComponentConfigurationException
     */
    void configureRoute(BaseRoute baseRoute, List<MessagingComponent> components) throws RouteConfigurationException, ComponentConfigurationException;
}


