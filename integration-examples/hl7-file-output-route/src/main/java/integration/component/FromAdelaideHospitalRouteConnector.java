package integration.component;

import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.connector.BaseInboundRouteConnector;
import integration.core.messaging.component.connector.FromRoute;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;

/**
 * Joins this route to the MLLP inbound route.
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-Route-Connector")
@FromRoute(connectorName = "mllpRouteConnector")
@ForwardingPolicy(name = "forwardAllMessages")
public class FromAdelaideHospitalRouteConnector extends BaseInboundRouteConnector {

    @Override
    public String getContentType() {
        return "HL7";
    }
}
