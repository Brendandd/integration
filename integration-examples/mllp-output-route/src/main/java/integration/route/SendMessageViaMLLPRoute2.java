package integration.route;

import org.springframework.beans.factory.annotation.Autowired;

import integration.component.Hl7MessageTypeFilter;
import integration.component.Hl7Splitter;
import integration.component.Hl7Transformer;
import integration.component.InboundRouteConnector1;
import integration.component.InboundRouteConnector2;
import integration.component.MLLPOutboundAdapter2;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.IntegrationRoute;
import jakarta.annotation.PostConstruct;

/**
 * An example route which accepts messages from an inbound route connector, filters, transforms and splits and then dispatches using
 * an outbound MLLP adapter.
 * 
 * This route shared the route connector and filter with the the other route in this example.  They both gets the same
 * messages from the inbound route connector but have different message processing.
 * 
 * This is an example of:
 *  1) multiple routes in the same module
 *  2) multiple routes receiving the same messages
 *  3) multiple routes sharing the same components
 *  4) multiple inbound route connectors
 * 
 * @author Brendan Douglas
 */
@IntegrationRoute(name = "Send-Message-Via-MLLP-2")
public class SendMessageViaMLLPRoute2 extends BaseRoute {

    @Autowired
    private InboundRouteConnector1 inboundRouteConnector1;

    @Autowired
    private Hl7Splitter splitter;

    @Autowired
    private Hl7Transformer transformer;

    @Autowired
    private Hl7MessageTypeFilter filter;

    @Autowired
    private MLLPOutboundAdapter2 mllpOutboundAdapter;
    
    @Autowired
    private InboundRouteConnector2 inboundRouteConnector2;

    // TODO accepts ACK from destination system.

    @Override
    @PostConstruct
    public void configureRoute() throws Exception {
        addInboundFlow(inboundRouteConnector1, transformer, filter);
        addInternalFlow(transformer, splitter);
        addInternalFlow(filter, splitter);
        addOutboundFlow(splitter, mllpOutboundAdapter);
        addDirectFlow(inboundRouteConnector2, mllpOutboundAdapter);
        
        applyConfiguration();
    }
}
