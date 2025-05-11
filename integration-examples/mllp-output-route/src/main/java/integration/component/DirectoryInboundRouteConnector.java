package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.annotation.IntegrationComponent;
import integration.core.messaging.component.type.connector.BaseInboundRouteConnector;
import integration.core.messaging.component.type.connector.annotation.From;

/**
 * Receives messages from the configured route.
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-Directory-Route-Connector")
@From(connectorName = "directoryRouteConnector")
@AllowedContentType(ContentTypeEnum.HL7)
public class DirectoryInboundRouteConnector extends BaseInboundRouteConnector {

}
