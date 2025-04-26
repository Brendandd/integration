package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.messaging.hl7.component.adapter.mllp.BaseMllpOutboundAdapter;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MllpOutboundAdapter extends BaseMllpOutboundAdapter {
    private static final String COMPONENT_NAME = "mllp-outbound";

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;

    public MllpOutboundAdapter() {
        super(COMPONENT_NAME);
    }

    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() {
        return messageAcceptancePolicy;
    }
}
