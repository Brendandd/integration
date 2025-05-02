package integration.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import integration.component.DirectoryInboundRouteConnector;
import integration.component.FromAdelaideHospitalRouteConnector;
import integration.component.Hl7MessageTypeFilter;
import integration.component.Hl7Splitter;
import integration.component.Hl7Transformation;
import integration.component.SydneyHospitalMLLPOutboundAdapter;
import integration.core.messaging.BaseRoute;
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
@Component
public class ToSydneyHospitalRoute extends BaseRoute {
    private static final String ROUTE_NAME = "Outbound-MLLP-to-Sydney-Hospital";

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

    public ToSydneyHospitalRoute() {
        super(ROUTE_NAME);
    }

    // TODO accepts ACK from destination system.

    @Override
    @PostConstruct
    public void configure() throws Exception {

        // Associate components to this route.
        addComponentToRoute(fromAdelaideHospitalInboundRouteConnector);
        addComponentToRoute(fromDirectoryInboundRouteConnector);
        addComponentToRoute(splitter);
        addComponentToRoute(transformation);
        addComponentToRoute(filter);
        addComponentToRoute(mllpOutboundAdapter);

        // Configure how the components are joined together.
        addInboundFlow(fromAdelaideHospitalInboundRouteConnector, transformation, filter);
        addInternalFlow(transformation, splitter);
        addInternalFlow(filter, splitter);
        addOutboundFlow(splitter, mllpOutboundAdapter);
        
        addDirectFlow(fromDirectoryInboundRouteConnector, mllpOutboundAdapter);
        
        // Start the route
        start();
    }
}
