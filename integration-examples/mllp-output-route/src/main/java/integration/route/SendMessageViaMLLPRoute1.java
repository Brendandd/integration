package integration.route;

import org.springframework.beans.factory.annotation.Autowired;

import integration.component.Hl7MessageTypeFilter;
import integration.component.InboundRouteConnector1;
import integration.component.MLLPOutboundAdapter1;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.IntegrationRoute;
import jakarta.annotation.PostConstruct;

/**
 * An example route which accepts messages from an inbound route connector, filters and then dispatches using
 * an outbound MLLP adapter.
 * 
 * This route shared the route connector and filter with the other route in this example.  They both gets the same
 * messages from the inbound route connector but have different message processing.
 * 
 * This is an example of:
 *  1) multiple routes in the same module
 *  2) multiple routes receiving the same messages
 *  3) multiple routes sharing the same components
 * 
 * @author Brendan Douglas
 */
@IntegrationRoute(name = "Send-Message-Via-MLLP-1")
public class SendMessageViaMLLPRoute1 extends BaseRoute {

    @Autowired
    private InboundRouteConnector1 inboundRouteConnector;

    @Autowired
    private Hl7MessageTypeFilter filter;

    @Autowired
    private MLLPOutboundAdapter1 mllpOutboundAdapter;

    @Override
    @PostConstruct
    public void configureRoute() throws Exception {
        addInboundFlow(inboundRouteConnector, filter);
        addOutboundFlow(filter, mllpOutboundAdapter);
        
        applyConfiguration();
    }
}
