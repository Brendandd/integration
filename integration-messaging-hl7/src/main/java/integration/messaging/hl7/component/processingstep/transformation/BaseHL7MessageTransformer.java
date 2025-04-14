package integration.messaging.hl7.component.processingstep.transformation;

import org.apache.camel.Exchange;

import integration.messaging.component.processingstep.transformation.MessageTransformer;
import integration.messaging.component.processingstep.transformation.TransformationException;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * Base class for all HL7 message transformers.
 */
public abstract class BaseHL7MessageTransformer extends MessageTransformer {

    @Override
    public String transformMessage(Exchange exchange, String messageBody) throws TransformationException {
        HL7Message sourceHL7Message = new HL7Message(messageBody);

        transform(exchange, sourceHL7Message);

        return sourceHL7Message.toString();
    }

    /**
     * Does the actual transformation.
     * 
     * @param source
     * @return
     */
    public abstract void transform(Exchange exchange, HL7Message source) throws TransformationException;
}
