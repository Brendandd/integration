package integration.messaging.hl7.component.handler.splitter;

import integration.messaging.component.handler.splitter.MessageSplitter;
import integration.messaging.component.handler.splitter.SplitterException;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * Base class for all HL7 message splitters.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseHL7MessageSplitter extends MessageSplitter {

    @Override
    public String[] splitMessage(String messageContent) throws SplitterException {
        HL7Message hl7Message = new HL7Message(messageContent);

        return convert(split(hl7Message));
    }

    private String[] convert(HL7Message[] source) {
        String[] destination = new String[source.length];

        for (int i = 0; i < source.length; i++) {
            destination[i] = source[i].toString();
        }

        return destination;
    }

    /**
     * Does the actual transformation.
     * 
     * @param source
     * @return
     */
    public abstract HL7Message[] split(HL7Message source) throws SplitterException;
}
