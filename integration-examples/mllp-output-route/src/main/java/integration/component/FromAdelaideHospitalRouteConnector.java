package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.annotation.IntegrationComponent;
import integration.core.messaging.component.type.connector.BaseInboundRouteConnector;
import integration.core.messaging.component.type.connector.annotation.From;

/**
 * Joins this route to the MLLP inbound route.
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-Route-Connector")
@From(connectorName = "mllpRouteConnector")
@AllowedContentType(ContentTypeEnum.HL7)
public class FromAdelaideHospitalRouteConnector extends BaseInboundRouteConnector {

}
