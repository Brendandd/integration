package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.connector.BaseInboundRouteConnector;
import integration.core.messaging.component.connector.FromRoute;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;

/**
 * Joins this route to the directory inbound route.
 * 
 * @author Brendan Douglas
 * 
 * 
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-Directory-Route-Connector")
@FromRoute(connectorName = "directoryRouteConnector")
@ForwardingPolicy(name = "forwardAllMessages")
@AllowedContentType(ContentTypeEnum.HL7)
public class DirectoryInboundRouteConnector extends BaseInboundRouteConnector {

}
