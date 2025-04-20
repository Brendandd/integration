package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.messaging.component.adapter.BaseRouteInboundConnector;
import integration.messaging.component.processingstep.filter.MessageForwardingPolicy;

/**
 * Joins this route to the MLLP inbound route.
 * 
 * @author Brendan Douglas
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MLLPInboundRouteConnector extends BaseRouteInboundConnector {
    private static final String COMPONENT_NAME = "MLLP-inbound-route-connector";

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    public MLLPInboundRouteConnector() {
        super(COMPONENT_NAME);
    }

    @Override
    public String getContentType() {
        return "HL7";
    }

    @Override
    public String getName() {
        return "mllpRouteConnector";
    }

    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        return messageForwardingPolicy;
    }
}
