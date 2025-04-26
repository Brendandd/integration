package integration.messaging.hl7.component.handler.transformation;

import integration.messaging.component.handler.transformation.MessageTransformer;
import integration.messaging.component.handler.transformation.TransformationException;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * Base class for all HL7 message transformers.
 */
public abstract class BaseHL7MessageTransformer extends MessageTransformer {

    @Override
    public String transformMessage(String messageBody) throws TransformationException {
        HL7Message sourceHL7Message = new HL7Message(messageBody);

        transform(sourceHL7Message);

        return sourceHL7Message.toString();
    }

    /**
     * Does the actual transformation.
     * 
     * @param source
     * @return
     */
    public abstract void transform(HL7Message source) throws TransformationException;
}
