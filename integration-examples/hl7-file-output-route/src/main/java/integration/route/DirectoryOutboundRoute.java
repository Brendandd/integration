package integration.route;

import org.springframework.beans.factory.annotation.Autowired;

import integration.component.DirectoryInboundRouteConnector;
import integration.component.FromAdelaideHospitalRouteConnector;
import integration.component.HL7DirectoryOutboundAdapter;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.IntegrationRoute;
import jakarta.annotation.PostConstruct;

/**
 * An example route which accepts messages from an 2 inbound route connector and directly sends the messages
 * to a directory outbound adapter.
 * 
 * 
 * This is an example of:
 *  1) multiple inbound route connectors.
 *  2)One of the messages sent to the inbound route connector comes from a directory adapter and the other
 *    comes from an MLLP adapter.
 * 
 * @author Brendan Douglas
 */
@IntegrationRoute(name = "Outbound-Directory-to-Sydney-Hospital")
public class DirectoryOutboundRoute extends BaseRoute {

    @Autowired
    private DirectoryInboundRouteConnector directoryInboundRouteConnector;
    
    @Autowired
    private FromAdelaideHospitalRouteConnector fromAdelaideHospitalInboundRouteConnector;

    @Autowired
    private HL7DirectoryOutboundAdapter hl7DirectoryOutboundAdapter;

    @Override
    @PostConstruct
    public void configureRoute() throws Exception {
        addDirectFlow(directoryInboundRouteConnector, hl7DirectoryOutboundAdapter);
        addDirectFlow(fromAdelaideHospitalInboundRouteConnector, hl7DirectoryOutboundAdapter);

        applyConfiguration();
    }
}
