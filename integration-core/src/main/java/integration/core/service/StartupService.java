package integration.core.service;

import java.util.List;

import integration.core.exception.AnnotationConfigurationException;
import integration.core.exception.ConfigurationException;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.component.MessagingComponent;

/**
 * A service to configure a route and components at startup.
 * 
 * @author Brendan Douglas
 *
 */
public interface StartupService {
    void configureRoute(BaseRoute baseRoute, List<MessagingComponent> components) throws ConfigurationException, AnnotationConfigurationException;
}


