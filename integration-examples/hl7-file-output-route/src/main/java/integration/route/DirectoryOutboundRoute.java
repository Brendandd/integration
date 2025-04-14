package integration.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import integration.component.DirectoryInboundRouteConnector;
import integration.component.HL7DirectoryOutboundCommunicationPoint;
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
    private HL7DirectoryOutboundCommunicationPoint outboundCommunicationPoint;

    public DirectoryOutboundRoute() {
        super(ROUTE_NAME);
    }

    @Override
    @PostConstruct
    public void configure() throws Exception {

        // Associate components to the this route.
        addComponentToRoute(directoryInboundRouteConnector);
        addComponentToRoute(outboundCommunicationPoint);

        // Configure how the components are joined together.
        addFlow(directoryInboundRouteConnector, outboundCommunicationPoint);

        // Start the route
        start();
    }
}
