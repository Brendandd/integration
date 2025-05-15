package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.annotation.IntegrationComponent;
import integration.core.runtime.messaging.component.type.connector.BaseInboundRouteConnector;
import integration.core.runtime.messaging.component.type.connector.annotation.From;

/**
 * Joins this route to the directory inbound route.
 * 
 * @author Brendan Douglas
 * 
 * 
 */
@IntegrationComponent(name = "Inbound-Route-Connector-2")
@From(connectorName = "abcd")
@AllowedContentType(ContentTypeEnum.HL7)
public class InboundRouteConnector2 extends BaseInboundRouteConnector {

}
