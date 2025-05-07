package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.connector.BaseInboundRouteConnector;
import integration.core.messaging.component.connector.FromRoute;
import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;

/**
 * Receives messages from the configured route.
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-Directory-Route-Connector")
@FromRoute(connectorName = "directoryRouteConnector")
public class DirectoryInboundRouteConnector extends BaseInboundRouteConnector {

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    @Override
    public String getContentType() {
        return "HL7";
    }

    
    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        return messageForwardingPolicy;
    }
}
