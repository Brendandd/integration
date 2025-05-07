package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.connector.BaseOutboundRouteConnector;
import integration.core.messaging.component.connector.ToRoute;
import integration.core.messaging.component.handler.filter.MessageAcceptancePolicy;

/**
 * An outbound route connector. Connects this route to another route.  This component does not care
 * what inbound components (if any) will receive the message.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "To-Other-Hospital-Route-Connector")
@ToRoute(connectorName = "mllpRouteConnector")
public class OutboundRouteConnector extends BaseOutboundRouteConnector {

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;


    @Override
    public String getContentType() {
        return "HL7";
    }

    
    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() {
        return messageAcceptancePolicy;
    }
}
