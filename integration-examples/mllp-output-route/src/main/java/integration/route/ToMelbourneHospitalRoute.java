package integration.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import integration.component.FromAdelaideHospitalRouteConnector;
import integration.component.Hl7MessageTypeFilter;
import integration.component.MelbourneHospitalMLLPOutboundAdapter;
import integration.core.messaging.BaseRoute;
import jakarta.annotation.PostConstruct;

/**
 * An example route which accepts messages from an inbound route connector, filters and then dispatches using
 * an outbound MLLP adapter.
 * 
 * This route shared the route connector and filter with the Sydney hospital route.  They both gets the same
 * messages from the inbound route connector but have different message processing.
 * 
 * This is an example of:
 *  1) multiple routes in the same module
 *  2) multiple routes receiving the same messages
 *  3) multiple routes sharing the same components
 * 
 * @author Brendan Douglas
 */
@Component
public class ToMelbourneHospitalRoute extends BaseRoute {
    private static final String ROUTE_NAME = "Outbound-MLLP-to-Melbourne-Hospital";

    @Autowired
    private FromAdelaideHospitalRouteConnector fromAdelaideHospitalInboundRouteConnector;

    @Autowired
    private Hl7MessageTypeFilter filter;

    @Autowired
    private MelbourneHospitalMLLPOutboundAdapter mllpOutboundAdapter;

    public ToMelbourneHospitalRoute() {
        super(ROUTE_NAME);
    }

    
    @Override
    @PostConstruct
    public void configureRoute() throws Exception {
        addInboundFlow(fromAdelaideHospitalInboundRouteConnector, filter);
        addOutboundFlow(filter, mllpOutboundAdapter);
        
        applyConfiguration();
    }
}
