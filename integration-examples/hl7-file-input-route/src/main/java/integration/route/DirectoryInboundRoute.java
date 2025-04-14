package integration.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import integration.component.HL7DirectoryInboundCommunicationPoint;
import integration.component.DirectoryOutboundRouteConnector;
import integration.messaging.BaseRoute;
import jakarta.annotation.PostConstruct;

/**
 * A route to accept a HL7 message from a directory.
 * 
 * @author Brendan Douglas
 */
@Component
public class DirectoryInboundRoute extends BaseRoute {
    public static final String ROUTE_NAME = "directory-inbound";

    @Autowired
    private HL7DirectoryInboundCommunicationPoint directoryInboundCommunicationPoint;

    @Autowired
    private DirectoryOutboundRouteConnector directoryOutboundRouteConnector;

    public DirectoryInboundRoute() {
        super(ROUTE_NAME);
    }

    @Override
    @PostConstruct
    public void configure() throws Exception {

        // Associate components to the this route.
        addComponentToRoute(directoryInboundCommunicationPoint);
        addComponentToRoute(directoryOutboundRouteConnector);

        // Configure how the components are joined together.
        addFlow(directoryInboundCommunicationPoint, directoryOutboundRouteConnector);

        start();
    }
}
