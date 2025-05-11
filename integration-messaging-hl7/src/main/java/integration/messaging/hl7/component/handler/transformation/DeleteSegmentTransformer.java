package integration.messaging.hl7.component.handler.transformation;

import integration.core.dto.MessageFlowDto;
import integration.core.messaging.component.type.handler.transformation.MessageTransformer;
import integration.core.messaging.component.type.handler.transformation.TransformationException;
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
    public String transformMessage(MessageFlowDto messageFlow) throws TransformationException {
     
        try {
            HL7Message source = new HL7Message(messageFlow.getMessage().getContent());
         
            source.removeAllSegments(getSegmentToDelete());
            
            return source.toString();
        } catch (Exception e) {
            throw new TransformationException("Error transforming the message", messageFlow.getId(), e);
        }
    }
}
