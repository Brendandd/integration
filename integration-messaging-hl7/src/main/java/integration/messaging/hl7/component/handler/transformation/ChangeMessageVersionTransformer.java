package integration.messaging.hl7.component.handler.transformation;

import integration.messaging.component.handler.transformation.TransformationException;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * A transformer to change the message version in a hl7 message.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class ChangeMessageVersionTransformer extends BaseHL7MessageTransformer {

    public abstract String getNewVersion() throws TransformationException;

    @Override
    public void transform(HL7Message source) throws TransformationException {

        try {
            source.changeMessageVersion(getNewVersion());
        } catch (Exception e) {
            throw new TransformationException("Error transforming the message", e);
        }
    }
}
