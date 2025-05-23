package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.annotation.IntegrationComponent;
import integration.core.runtime.messaging.component.type.connector.BaseInboundRouteConnectorComponent;
import integration.core.runtime.messaging.component.type.connector.annotation.From;

/**
 * Receives messages from an outbound route connector with connector name "xyx"
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "Inbound-Route-Connector-1")
@From(connectorName = "xyz")
@AllowedContentType(ContentTypeEnum.HL7)
public class InboundRouteConnector1 extends BaseInboundRouteConnectorComponent {

}
