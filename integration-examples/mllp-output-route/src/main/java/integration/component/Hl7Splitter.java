package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.messaging.component.handler.filter.MessageForwardingPolicy;
import integration.messaging.component.handler.splitter.BaseSplitterProcessingStep;
import integration.messaging.component.handler.splitter.MessageSplitter;

/**
 * A message splitter. Duplicates the message based on the number of OBX
 * segments.
 * 
 * @author Brendan Douglas
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Hl7Splitter extends BaseSplitterProcessingStep {
    private static final String CONTENT_TYPE = "HL7";
    private static final String COMPONENT_NAME = "hl7-splitter";

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;

    @Autowired
    @Qualifier("splitOnOXBSegments")
    private MessageSplitter messageSplitter;

    public Hl7Splitter() {
        super(COMPONENT_NAME);
    }

    @Override
    public MessageSplitter getSplitter() {
        return messageSplitter;
    }

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
