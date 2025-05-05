package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.messaging.component.connector.BaseOutboundRouteConnector;
import integration.core.messaging.component.handler.filter.MessageAcceptancePolicy;

/**
 * An outbound route connector. Connects this route to another route.
 * 
 * @author Brendan Douglas
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DirectoryOutboundRouteConnector extends BaseOutboundRouteConnector {
    private static final String COMPONENT_NAME = "From-Adelaide-Hospital-Directory-Inbound-Adapter";

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;

    @Override
    public String getContentType() {
        return "HL7";
    }
    
    @Override
    public String getConnectorName() {
        return "directoryRouteConnector";
    }

    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() {
        return messageAcceptancePolicy;
    }
    
    @Override
    public String getName() {
        return COMPONENT_NAME;
    }
}
