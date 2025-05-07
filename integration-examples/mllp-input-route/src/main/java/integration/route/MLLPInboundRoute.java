package integration.route;

import org.springframework.beans.factory.annotation.Autowired;

import integration.component.FromAdelaideHospitalMLLPInboundAdapter;
import integration.component.OutboundRouteConnector;
import integration.core.messaging.BaseRoute;
import integration.core.messaging.IntegrationRoute;
import jakarta.annotation.PostConstruct;

/**
 * An example route which accepts messages from an inbound MLLP adapter and passes them directly to an outbound route connector.  This route
 * does not know what other routes (if any) are expecting to receive messages from the route connector.
 * 
 * In the MLLPOutboundRoute module there are 2 routes which have been configured to receive messages from this route.
 * 
 * 
 * @author Brendan Douglas
 */
@IntegrationRoute(name = "Inbound-MLLP-from-Adelaide-Hospital")
public class MLLPInboundRoute extends BaseRoute {

    @Autowired
    private FromAdelaideHospitalMLLPInboundAdapter mllpInboundAdapter;

    @Autowired
    private OutboundRouteConnector toMllpOutboundRouteConnector;
    
    @Override
    @PostConstruct
    public void configureRoute() throws Exception {
        addDirectFlow(mllpInboundAdapter, toMllpOutboundRouteConnector);
        
        applyConfiguration();
    }
}
