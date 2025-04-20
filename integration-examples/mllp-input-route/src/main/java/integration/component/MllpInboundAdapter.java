package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.messaging.component.processingstep.filter.MessageForwardingPolicy;
import integration.messaging.hl7.component.adapter.mllp.BaseMllpInboundAdapter;

/**
 * An MLLP inbound communication point.
 * 
 * @author Brendan Douglas
 */
@Component("mllpInboundAdapter")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MllpInboundAdapter extends BaseMllpInboundAdapter {
    private static final String COMPONENT_NAME = "mllp-inbound";

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    public MllpInboundAdapter() throws Exception {
        super(COMPONENT_NAME);
    }

    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        return messageForwardingPolicy;
    }
}
