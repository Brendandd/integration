package integration.messaging.hl7.component.handler.transformation;

import integration.core.dto.MessageFlowStepDto;
import integration.core.messaging.component.handler.transformation.MessageTransformer;
import integration.core.messaging.component.handler.transformation.TransformationException;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * A transformer to change the message version in a hl7 message.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class ChangeMessageVersionTransformer extends MessageTransformer {

    public abstract String getNewVersion() throws TransformationException;

    @Override
    public String transformMessage(MessageFlowStepDto messageFlowStep) throws TransformationException {

        try {
            HL7Message source = new HL7Message(messageFlowStep.getMessage().getContent());
            
            source.changeMessageVersion(getNewVersion());
            
            return source.toString();
        } catch (Exception e) {
            throw new TransformationException("Error transforming the message", e);
        }
    }
}
