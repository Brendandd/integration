package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.connector.BaseInboundRouteConnector;
import integration.core.messaging.component.connector.From;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;

/**
 * Receives messages from the configured route.
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-Directory-Route-Connector")
@From(connectorName = "directoryRouteConnector")
@ForwardingPolicy(name = "forwardAllMessages")
@AllowedContentType(ContentTypeEnum.HL7)
public class DirectoryInboundRouteConnector extends BaseInboundRouteConnector {

}
