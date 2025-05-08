package integration.component;

import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.connector.BaseOutboundRouteConnector;
import integration.core.messaging.component.connector.ToRoute;
import integration.core.messaging.component.handler.filter.AcceptancePolicy;

/**
 * An outbound route connector. Connects this route to another route.  This component does not care
 * what inbound components (if any) will receive the message.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "To-Other-Hospital-Route-Connector")
@ToRoute(connectorName = "mllpRouteConnector")
@AcceptancePolicy(name = "acceptAllMessages")
public class OutboundRouteConnector extends BaseOutboundRouteConnector {

    @Override
    public String getContentType() {
        return "HL7";
    }
}
