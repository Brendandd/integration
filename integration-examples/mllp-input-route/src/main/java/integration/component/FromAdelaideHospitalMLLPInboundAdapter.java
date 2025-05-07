package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;
import integration.messaging.hl7.component.adapter.mllp.BaseMllpInboundAdapter;

/**
 * An MLLP inbound adapter.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-MLLP-Inbound-Adapter")
public class FromAdelaideHospitalMLLPInboundAdapter extends BaseMllpInboundAdapter {

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        return messageForwardingPolicy;
    }
}
