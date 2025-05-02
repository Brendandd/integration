package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.messaging.component.connector.BaseInboundRouteConnector;
import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;

/**
 * Receives messages from the configured route.
 * 
 * @author Brendan Douglas
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DirectoryInboundRouteConnector extends BaseInboundRouteConnector {
    private static final String COMPONENT_NAME = "From-Adelaide-Hospital-Directory-Route-Connector";

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    @Override
    public String getContentType() {
        return "HL7";
    }

    @Override
    public String getConnectorName() {
        return "AdelaideHospitalDirectoryRoute"; // receives messages from an outbound connector which sets the same name.
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
