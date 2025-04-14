package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.messaging.component.processingstep.filter.MessageAcceptancePolicy;
import integration.messaging.hl7.component.communicationpoint.mllp.BaseMllpOutboundCommunicationPoint;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MllpOutboundCommunicationPoint extends BaseMllpOutboundCommunicationPoint {
    private static final String COMPONENT_NAME = "mllp-outbound";

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;

    public MllpOutboundCommunicationPoint() {
        super(COMPONENT_NAME);
    }

    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() {
        return messageAcceptancePolicy;
    }
}
