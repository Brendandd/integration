package integration.core.messaging.component.handler.transformation;

import integration.core.dto.MessageFlowStepDto;

/**
 * Interface for all transformers.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageTransformer {
    
    public String transform(MessageFlowStepDto messageFlowStep) throws TransformationException {
        try {
            return transformMessage(messageFlowStep);
        } catch (Exception e) {
            throw new TransformationException("Error transforming the message", e);
        }
    }

    public abstract String transformMessage(MessageFlowStepDto messageFlowStep) throws TransformationException, Exception;
}
