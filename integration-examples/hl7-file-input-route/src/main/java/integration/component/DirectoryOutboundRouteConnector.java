package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.connector.BaseOutboundRouteConnector;
import integration.core.messaging.component.connector.ToRoute;
import integration.core.messaging.component.handler.filter.MessageAcceptancePolicy;

/**
 * An outbound route connector. Connects this route to another route.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-Directory-Inbound-Adapter")
@ToRoute(connectorName = "directoryRouteConnector")
public class DirectoryOutboundRouteConnector extends BaseOutboundRouteConnector {

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
