package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.annotation.IntegrationComponent;
import integration.core.runtime.messaging.component.type.connector.BaseOutboundRouteConnectorComponent;
import integration.core.runtime.messaging.component.type.connector.annotation.StaticDestination;

/**
 * An outbound route connector. Connects this route to another route.  This component does not care
 * what inbound components (if any) will receive the message.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "Outbound-Route-Connector")
@StaticDestination(connectorName = "xyz")
@AllowedContentType(ContentTypeEnum.HL7)
public class OutboundRouteConnector extends BaseOutboundRouteConnectorComponent {

}
