package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;
import integration.core.messaging.component.handler.transformation.BaseTransformationProcessingStep;
import integration.core.messaging.component.handler.transformation.MessageTransformer;

@IntegrationComponent(name = "Transform-to-version-2-5")
public class Hl7Transformation extends BaseTransformationProcessingStep {
    private static final String CONTENT_TYPE = "HL7";

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;

    @Autowired
    @Qualifier("changeVersionTo2.5")
    private MessageTransformer messageTransformer;

    @Override
    public MessageTransformer getTransformer() {
        return messageTransformer;
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
