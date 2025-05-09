package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.connector.BaseOutboundRouteConnector;
import integration.core.messaging.component.connector.StaticDestination;

/**
 * An outbound route connector. Connects this route to another route.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-Directory-Inbound-Adapter")
@StaticDestination(connectorName = "directoryRouteConnector")
@AllowedContentType(ContentTypeEnum.HL7)
public class DirectoryOutboundRouteConnector extends BaseOutboundRouteConnector {
        
}
