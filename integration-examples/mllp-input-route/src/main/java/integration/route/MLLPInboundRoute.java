package integration.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import integration.component.MllpInboundCommunicationPoint;
import integration.component.MLLPOutboundRouteConnector;
import integration.messaging.BaseRoute;
import jakarta.annotation.PostConstruct;

/**
 * A route.
 * 
 * @author Brendan Douglas
 */
@Component
public class MLLPInboundRoute extends BaseRoute {
    public static final String ROUTE_NAME = "mllp-inbound";

    @Autowired
    private MllpInboundCommunicationPoint mllpInboundCommunicationPoint;

    @Autowired
    private MLLPOutboundRouteConnector toMllpOutboundRouteConnector;

    public MLLPInboundRoute() {
        super(ROUTE_NAME);
    }

    @Override
    @PostConstruct
    public void configure() throws Exception {

        // Associate components to the this route.
        addComponentToRoute(mllpInboundCommunicationPoint);
        addComponentToRoute(toMllpOutboundRouteConnector);

        // Configure how the components are joined together.
        addFlow(mllpInboundCommunicationPoint, toMllpOutboundRouteConnector);

        start();
    }
}
