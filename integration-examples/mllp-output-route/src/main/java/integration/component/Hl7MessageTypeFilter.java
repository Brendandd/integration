package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.messaging.component.handler.filter.BaseFilterProcessingStep;
import integration.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.messaging.component.handler.filter.MessageForwardingPolicy;

/**
 * HL7 message type filter compoment.
 * 
 * @author Brendan Douglas
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Hl7MessageTypeFilter extends BaseFilterProcessingStep {

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    @Autowired
    @Qualifier("acceptADT^A04")
    private MessageAcceptancePolicy messageAcceptancePolicy;

    private static final String COMPONENT_NAME = "hl7-filter";

    public Hl7MessageTypeFilter() {
        super(COMPONENT_NAME);
    }

    private static final String CONTENT_TYPE = "HL7";

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() {
        return messageAcceptancePolicy;
    }

    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        return messageForwardingPolicy;
    }
}
