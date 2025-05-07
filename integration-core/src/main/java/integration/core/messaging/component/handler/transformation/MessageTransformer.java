package integration.core.messaging.component.handler.transformation;

import integration.core.dto.MessageFlowDto;

/**
 * Interface for all transformers.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageTransformer {
    
    public String transform(MessageFlowDto messageFlow) throws TransformationException {
        try {
            return transformMessage(messageFlow);
        } catch (Exception e) {
            throw new TransformationException("Error transforming the message", e);
        }
    }

    public abstract String transformMessage(MessageFlowDto messageFlow) throws TransformationException, Exception;
}
