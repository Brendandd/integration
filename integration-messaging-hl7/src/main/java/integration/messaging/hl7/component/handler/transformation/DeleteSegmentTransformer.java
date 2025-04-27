package integration.messaging.hl7.component.handler.transformation;

import integration.core.dto.MessageFlowStepDto;
import integration.messaging.component.handler.transformation.MessageTransformer;
import integration.messaging.component.handler.transformation.TransformationException;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * A transformer to delete all matching segments in a hl7 message.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class DeleteSegmentTransformer extends MessageTransformer {

    public abstract String getSegmentToDelete();

    @Override
    public String transformMessage(MessageFlowStepDto messageFlowStep) throws TransformationException {
     
        try {
            HL7Message source = new HL7Message(messageFlowStep.getMessage().getContent());
         
            source.removeAllSegments(getSegmentToDelete());
            
            return source.toString();
        } catch (Exception e) {
            throw new TransformationException("Error transforming the message", e);
        }
    }
}
