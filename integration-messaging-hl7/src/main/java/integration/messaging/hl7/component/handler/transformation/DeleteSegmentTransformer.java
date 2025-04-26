package integration.messaging.hl7.component.handler.transformation;

import integration.messaging.component.handler.transformation.TransformationException;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * A transformer to delete all matching segments in a hl7 message.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class DeleteSegmentTransformer extends BaseHL7MessageTransformer {

    public abstract String getSegmentToDelete();

    @Override
    public void transform(HL7Message source) throws TransformationException {
        try {
            source.removeAllSegments(getSegmentToDelete());
        } catch (Exception e) {
            throw new TransformationException("Error transforming the message", e);
        }
    }
}
