package integration.messaging.component.connector;

import integration.messaging.component.BaseMessagingComponent;

/**
 * Base class for components designed to connect routes together.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseRouteConnector extends BaseMessagingComponent {
    
    public BaseRouteConnector(String componentName) {
        super(componentName);
    }
    
    
    /**
     * The name used to connect an inbound connectors to an outbound connector.
     * 
     * @return
     */
    public abstract String getName();
}
