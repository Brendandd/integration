package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.messaging.component.connector.BaseOutboundRouteConnector;
import integration.messaging.component.handler.filter.MessageAcceptancePolicy;

/**
 * An outbound route connector. Connects this route to another route.
 * 
 * @author Brendan Douglas
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DirectoryOutboundRouteConnector extends BaseOutboundRouteConnector {

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;

    private static final String COMPONENT_NAME = "directory-outbound-route-connector";

    public DirectoryOutboundRouteConnector() throws Exception {
        super(COMPONENT_NAME);
    }

    @Override
    public String getContentType() {
        return "HL7";
    }

    @Override
    public String getName() {
        return "directoryRouteConnector";
    }

    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() {
        return messageAcceptancePolicy;
    }
}
