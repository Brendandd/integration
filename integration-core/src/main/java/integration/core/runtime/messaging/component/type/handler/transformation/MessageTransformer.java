package integration.core.runtime.messaging.component.type.handler.transformation;

import integration.core.dto.MessageFlowDto;

/**
 * Base class for all transformers.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageTransformer {
    
    public String transform(MessageFlowDto messageFlow) throws TransformationException {
        try {
            return transformMessage(messageFlow);
        } catch (Exception e) {
            throw new TransformationException("Error transforming the message", messageFlow.getId(), e);
        }
    }

    public abstract String transformMessage(MessageFlowDto messageFlow) throws TransformationException;
}
