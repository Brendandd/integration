package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.messaging.hl7.component.adapter.mllp.BaseMllpOutboundAdapter;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MelbourneHospitalMLLPOutboundAdapter extends BaseMllpOutboundAdapter {
    private static final String COMPONENT_NAME = "To-Melbourne-Hospital-MLLP-Outbound-Adapter";

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;

    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() {
        return messageAcceptancePolicy;
    }
    
    @Override
    public String getName() {
        return COMPONENT_NAME;
    }
}
