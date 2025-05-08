package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.connector.BaseInboundRouteConnector;
import integration.core.messaging.component.connector.From;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;

/**
 * Joins this route to the MLLP inbound route.
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-Route-Connector")
@From(connectorName = "mllpRouteConnector")
@ForwardingPolicy(name = "forwardAllMessages")
@AllowedContentType(ContentTypeEnum.HL7)
public class FromAdelaideHospitalRouteConnector extends BaseInboundRouteConnector {

}
