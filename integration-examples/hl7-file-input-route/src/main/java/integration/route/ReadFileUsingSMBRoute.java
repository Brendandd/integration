package integration.route;

import org.springframework.beans.factory.annotation.Autowired;

import integration.component.OutboundRouteConnector;
import integration.component.SMBInboundAdapter;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.IntegrationRoute;
import jakarta.annotation.PostConstruct;

/**
 * An example route which reads a file from a directory and adds it directly to an outbound route connector.
 * 
 * @author Brendan Douglas
 */
@IntegrationRoute(name = "Read-File-Using-SMB")
public class ReadFileUsingSMBRoute extends BaseRoute {

    @Autowired
    private SMBInboundAdapter smbInboundAdapter;

    @Autowired
    private OutboundRouteConnector outboundRouteConnector;

    @Override
    @PostConstruct
    public void configureRoute() throws Exception {
        addDirectFlow(smbInboundAdapter, outboundRouteConnector);

        applyConfiguration();
    }
}
