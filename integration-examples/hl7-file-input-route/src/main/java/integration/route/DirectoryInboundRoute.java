package integration.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import integration.component.DirectoryOutboundRouteConnector;
import integration.component.HL7DirectoryInboundAdapter;
import integration.core.messaging.BaseRoute;
import jakarta.annotation.PostConstruct;

/**
 * An example route which reads a file from a directory and adds it directly to an outbound route connector.
 * 
 * @author Brendan Douglas
 */
@Component
public class DirectoryInboundRoute extends BaseRoute {
    public static final String ROUTE_NAME = "Inbound-Directory-from-Adelaide-Hospital";

    @Autowired
    private HL7DirectoryInboundAdapter directoryInboundAdapter;

    @Autowired
    private DirectoryOutboundRouteConnector directoryOutboundRouteConnector;

    public DirectoryInboundRoute() {
        super(ROUTE_NAME);
    }

    @Override
    @PostConstruct
    public void configure() throws Exception {

        // Associate components to the this route.
        addComponentToRoute(directoryInboundAdapter);
        addComponentToRoute(directoryOutboundRouteConnector);

        // Configure how the components are joined together.
        addDirectFlow(directoryInboundAdapter, directoryOutboundRouteConnector);

        start();
    }
}
