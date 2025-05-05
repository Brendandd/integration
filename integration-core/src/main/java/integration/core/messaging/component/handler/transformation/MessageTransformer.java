package integration.core.messaging.component.handler.transformation;

import org.springframework.beans.factory.annotation.Autowired;

import integration.core.dto.MessageFlowStepDto;
import integration.core.service.MetaDataService;

/**
 * Interface for all transformers.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageTransformer {
    
    @Autowired
    protected MetaDataService metaDataService;
    

    public String transform(MessageFlowStepDto messageFlowStep) throws TransformationException {
        try {
            return transformMessage(messageFlowStep);
        } catch (Exception e) {
            throw new TransformationException("Error transforming the message", e);
        }
    }

    public abstract String transformMessage(MessageFlowStepDto messageFlowStep) throws TransformationException, Exception;
}
