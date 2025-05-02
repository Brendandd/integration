package integration.core.messaging.component.connector;

import integration.core.messaging.component.BaseMessagingComponent;

/**
 * Base class for components designed to connect routes together.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseRouteConnector extends BaseMessagingComponent {
    
    /**
     * The name used to connect an inbound connectors to an outbound connector.
     * 
     * @return
     */
    public abstract String getConnectorName();
}
