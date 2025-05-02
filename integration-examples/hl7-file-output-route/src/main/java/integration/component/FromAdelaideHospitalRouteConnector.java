package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.messaging.component.connector.BaseInboundRouteConnector;
import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;

/**
 * Joins this route to the MLLP inbound route.
 * 
 * @author Brendan Douglas
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FromAdelaideHospitalRouteConnector extends BaseInboundRouteConnector {
    private static final String COMPONENT_NAME = "From-Adelaide-Hospital-Route-Connector";

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    @Override
    public String getContentType() {
        return "HL7";
    }

    @Override
    public String getConnectorName() {
        return "mllpRouteConnector";
    }

    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        return messageForwardingPolicy;
    }
    
    @Override
    public String getName() {
        return COMPONENT_NAME;
    }
}
