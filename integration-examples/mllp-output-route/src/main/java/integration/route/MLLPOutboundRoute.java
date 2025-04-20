package integration.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import integration.component.DirectoryInboundRouteConnector;
import integration.component.MLLPInboundRouteConnector;
import integration.component.Hl7MessageTypeFilter;
import integration.component.Hl7Splitter;
import integration.component.Hl7Transformation;
import integration.component.MllpOutboundAdapter;
import integration.messaging.BaseRoute;
import jakarta.annotation.PostConstruct;

/**
 * An outbound route. Sends the message externally.
 * 
 * This route accepts messages from both the mllp-input-route and the
 * hl7-file-input-route.
 * 
 * @author Brendan Douglas
 */
@Component
public class MLLPOutboundRoute extends BaseRoute {
    private static final String ROUTE_NAME = "mllp-outbound";

    @Autowired
    private MLLPInboundRouteConnector fromMllpInboundRouteConnector;

    @Autowired
    private DirectoryInboundRouteConnector fromDirectoryInboundRouteConnector;

    @Autowired
    private Hl7Splitter splitter;

    @Autowired
    private Hl7Transformation transformation;

    @Autowired
    private Hl7MessageTypeFilter filter;

    @Autowired
    private MllpOutboundAdapter mllpOutboundAdapter;

    public MLLPOutboundRoute() {
        super(ROUTE_NAME);
    }

    // TODO accepts ACK from destination system.

    @Override
    @PostConstruct
    public void configure() throws Exception {

        // Associate components to the this route.
        addComponentToRoute(fromMllpInboundRouteConnector);
        addComponentToRoute(fromDirectoryInboundRouteConnector);
        addComponentToRoute(splitter);
        addComponentToRoute(transformation);
        addComponentToRoute(filter);
        addComponentToRoute(mllpOutboundAdapter);

        // Configure how the components are joined together.
        addFlow(fromMllpInboundRouteConnector, transformation, filter);
        addFlow(fromDirectoryInboundRouteConnector, mllpOutboundAdapter);
        addFlow(transformation, splitter);
        addFlow(filter, splitter);
        addFlow(splitter, mllpOutboundAdapter);

        // Start the route
        start();
    }
}
