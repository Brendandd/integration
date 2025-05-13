package integration.route;

import org.springframework.beans.factory.annotation.Autowired;

import integration.component.DirectoryInboundRouteConnector;
import integration.component.FromAdelaideHospitalRouteConnector;
import integration.component.Hl7MessageTypeFilter;
import integration.component.Hl7Splitter;
import integration.component.Hl7Transformation;
import integration.component.SydneyHospitalMLLPOutboundAdapter;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.IntegrationRoute;
import jakarta.annotation.PostConstruct;

/**
 * An example route which accepts messages from an inbound route connector, filters, transforms and splits and then dispatches using
 * an outbound MLLP adapter.
 * 
 * This route shared the route connector and filter with the Melbourne hospital route.  They both gets the same
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
@IntegrationRoute(name = "Outbound-MLLP-to-Sydney-Hospital")
public class ToSydneyHospitalRoute extends BaseRoute {

    @Autowired
    private FromAdelaideHospitalRouteConnector fromAdelaideHospitalInboundRouteConnector;

    @Autowired
    private Hl7Splitter splitter;

    @Autowired
    private Hl7Transformation transformation;

    @Autowired
    private Hl7MessageTypeFilter filter;

    @Autowired
    private SydneyHospitalMLLPOutboundAdapter mllpOutboundAdapter;
    
    @Autowired
    private DirectoryInboundRouteConnector fromDirectoryInboundRouteConnector;

    // TODO accepts ACK from destination system.

    @Override
    @PostConstruct
    public void configureRoute() throws Exception {
        addInboundFlow(fromAdelaideHospitalInboundRouteConnector, transformation, filter);
        addInternalFlow(transformation, splitter);
        addInternalFlow(filter, splitter);
        addOutboundFlow(splitter, mllpOutboundAdapter);
        addDirectFlow(fromDirectoryInboundRouteConnector, mllpOutboundAdapter);
        
        applyConfiguration();
    }
}
