package integration.route;

import org.springframework.beans.factory.annotation.Autowired;

import integration.component.InboundRouteConnector1;
import integration.component.InboundRouteConnector2;
import integration.component.SMBOutboundAdapter;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.IntegrationRoute;
import jakarta.annotation.PostConstruct;

/**
 * An example route which accepts messages from  2 inbound route connector and directly sends the messages
 * to a SMB outbound adapter.
 * 
 * 
 * This is an example of:
 *  1) multiple inbound route connectors sending messages to the same outbound adapter.
 * 
 * @author Brendan Douglas
 */
@IntegrationRoute(name = "Write-File-Using-SMB")
public class WriteFileUsingSMBRoute extends BaseRoute {

    @Autowired
    private InboundRouteConnector2 inboundRouteConnector2;
    
    @Autowired
    private InboundRouteConnector1 inboundRouteConnector1;

    @Autowired
    private SMBOutboundAdapter smbOutboundAdapter;

    @Override
    @PostConstruct
    public void configureRoute() throws Exception {
        addDirectFlow(inboundRouteConnector1, smbOutboundAdapter);
        addDirectFlow(inboundRouteConnector2, smbOutboundAdapter);

        applyConfiguration();
    }
}
