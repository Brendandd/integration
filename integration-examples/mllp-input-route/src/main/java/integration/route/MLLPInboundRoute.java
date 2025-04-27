package integration.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import integration.component.MLLPOutboundRouteConnector;
import integration.component.MllpInboundAdapter;
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
    private MllpInboundAdapter mllpInboundAdapter;

    @Autowired
    private MLLPOutboundRouteConnector toMllpOutboundRouteConnector;

    public MLLPInboundRoute() {
        super(ROUTE_NAME);
    }

    @Override
    @PostConstruct
    public void configure() throws Exception {

        // Associate components to the this route.
        addComponentToRoute(mllpInboundAdapter);
        addComponentToRoute(toMllpOutboundRouteConnector);

        // Add a direct flow which is am inbound component directly to an outbound component.
        addDirectFlow(mllpInboundAdapter, toMllpOutboundRouteConnector);

        start();
    }
}
