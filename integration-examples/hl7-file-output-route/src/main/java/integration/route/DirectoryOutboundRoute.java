package integration.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import integration.component.DirectoryInboundRouteConnector;
import integration.component.HL7DirectoryOutboundAdapter;
import integration.messaging.BaseRoute;
import jakarta.annotation.PostConstruct;

/**
 * An outbound route. Sends the message externally.
 * 
 * This route accepts messages from hl7-file-input-route.
 * 
 * @author Brendan Douglas
 */
@Component
public class DirectoryOutboundRoute extends BaseRoute {
    private static final String ROUTE_NAME = "directory-outbound";

    @Autowired
    private DirectoryInboundRouteConnector directoryInboundRouteConnector;

    @Autowired
    private HL7DirectoryOutboundAdapter hl7DirectoryOutboundAdapter;

    public DirectoryOutboundRoute() {
        super(ROUTE_NAME);
    }

    @Override
    @PostConstruct
    public void configure() throws Exception {

        // Associate components to the this route.
        addComponentToRoute(directoryInboundRouteConnector);
        addComponentToRoute(hl7DirectoryOutboundAdapter);

        // Configure how the components are joined together.
        addDirectFlow(directoryInboundRouteConnector, hl7DirectoryOutboundAdapter);

        // Start the route
        start();
    }
}
