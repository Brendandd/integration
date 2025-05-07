package integration.route;

import org.springframework.beans.factory.annotation.Autowired;

import integration.component.DirectoryOutboundRouteConnector;
import integration.component.HL7DirectoryInboundAdapter;
import integration.core.messaging.BaseRoute;
import integration.core.messaging.IntegrationRoute;
import jakarta.annotation.PostConstruct;

/**
 * An example route which reads a file from a directory and adds it directly to an outbound route connector.
 * 
 * @author Brendan Douglas
 */
@IntegrationRoute(name = "Inbound-Directory-from-Adelaide-Hospital")
public class DirectoryInboundRoute extends BaseRoute {

    @Autowired
    private HL7DirectoryInboundAdapter directoryInboundAdapter;

    @Autowired
    private DirectoryOutboundRouteConnector directoryOutboundRouteConnector;

    @Override
    @PostConstruct
    public void configureRoute() throws Exception {
        addDirectFlow(directoryInboundAdapter, directoryOutboundRouteConnector);

        applyConfiguration();
    }
}
