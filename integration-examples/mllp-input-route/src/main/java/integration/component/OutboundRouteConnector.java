package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.annotation.IntegrationComponent;
import integration.core.runtime.messaging.component.type.connector.BaseOutboundRouteConnector;
import integration.core.runtime.messaging.component.type.connector.annotation.StaticDestination;

/**
 * An outbound route connector. Connects this route to another route.  This component does not care
 * what inbound components (if any) will receive the message.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "To-Other-Hospital-Route-Connector")
@StaticDestination(connectorName = "mllpRouteConnector")
@AllowedContentType(ContentTypeEnum.HL7)
public class OutboundRouteConnector extends BaseOutboundRouteConnector {

}
