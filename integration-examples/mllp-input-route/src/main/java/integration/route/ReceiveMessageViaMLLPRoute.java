package integration.route;

import org.springframework.beans.factory.annotation.Autowired;

import integration.component.MLLPInboundAdapter;
import integration.component.OutboundRouteConnector;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.IntegrationRoute;
import jakarta.annotation.PostConstruct;

/**
 * An example route which accepts messages from an inbound MLLP adapter and passes them directly to an outbound route connector.  This route
 * does not know what other routes (if any) are expecting to receive messages from the route connector.
 * 
 * In the MLLPOutboundRoute module there are 2 routes which have been configured to receive messages from this route.
 * 
 * @author Brendan Douglas
 */
@IntegrationRoute(name = "Receive-Message-Via-MLLP")
public class ReceiveMessageViaMLLPRoute extends BaseRoute {

    @Autowired
    private MLLPInboundAdapter mllpInboundAdapter;

    @Autowired
    private OutboundRouteConnector outboundRouteConnector;
    
    @Override
    @PostConstruct
    public void configureRoute() throws Exception {
        addDirectFlow(mllpInboundAdapter, outboundRouteConnector);
        
        applyConfiguration();
    }
}
