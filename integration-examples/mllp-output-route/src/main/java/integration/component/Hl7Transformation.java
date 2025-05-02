package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;
import integration.core.messaging.component.handler.transformation.BaseTransformationProcessingStep;
import integration.core.messaging.component.handler.transformation.MessageTransformer;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Hl7Transformation extends BaseTransformationProcessingStep {
    private static final String CONTENT_TYPE = "HL7";
    private static final String COMPONENT_NAME = "Transform-to-version-2-5";

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
    
    @Override
    public String getName() {
        return COMPONENT_NAME;
    }
}
