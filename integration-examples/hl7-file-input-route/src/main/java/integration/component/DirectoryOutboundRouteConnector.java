package integration.component;

import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.connector.BaseOutboundRouteConnector;
import integration.core.messaging.component.connector.ToRoute;
import integration.core.messaging.component.handler.filter.AcceptancePolicy;

/**
 * An outbound route connector. Connects this route to another route.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-Directory-Inbound-Adapter")
@ToRoute(connectorName = "directoryRouteConnector")
@AcceptancePolicy(name = "acceptAllMessages")
public class DirectoryOutboundRouteConnector extends BaseOutboundRouteConnector {
        
    @Override
    public String getContentType() {
        return "HL7";
    }
}
