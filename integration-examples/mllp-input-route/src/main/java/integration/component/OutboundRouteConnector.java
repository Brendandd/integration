package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.messaging.component.connector.BaseOutboundRouteConnector;
import integration.core.messaging.component.handler.filter.MessageAcceptancePolicy;

/**
 * An outbound route connector. Connects this route to another route.  This component does not care
 * what inbound components (if any) will receive the message.
 * 
 * @author Brendan Douglas
 */
@Component("mllpOutboundRouteConnector")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OutboundRouteConnector extends BaseOutboundRouteConnector {
    private static final String COMPONENT_NAME = "To-Other-Hospital-Route-Connector";

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;


    @Override
    public String getContentType() {
        return "HL7";
    }

    @Override
    public String getConnectorName() {
        return "mllpRouteConnector";
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
