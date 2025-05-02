package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;
import integration.messaging.hl7.component.adapter.mllp.BaseMllpInboundAdapter;

/**
 * An MLLP inbound adapter.
 * 
 * @author Brendan Douglas
 */
@Component("mllpInboundAdapter")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FromAdelaideHospitalMLLPInboundAdapter extends BaseMllpInboundAdapter {
    private static final String COMPONENT_NAME = "From-Adelaide-Hospital-MLLP-Inbound-Adapter";

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        return messageForwardingPolicy;
    }
    
    @Override
    public String getName() {
        return COMPONENT_NAME;
    }
}
