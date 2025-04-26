package integration.component;

import org.springframework.stereotype.Component;

import integration.messaging.component.handler.splitter.SplitterException;
import integration.messaging.hl7.component.handler.splitter.BaseHL7MessageSplitter;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * OBX segment splitter logic.
 */
@Component("splitOnOXBSegments")
public class SplitBasedOnOBX extends BaseHL7MessageSplitter {

    @Override
    public HL7Message[] split(HL7Message hl7Message) throws SplitterException {
        try {
            int obxCount = hl7Message.getSegmentCount("OBX");

            HL7Message[] messageFlowArray = new HL7Message[obxCount];

            for (int i = 0; i < obxCount; i++) {
                messageFlowArray[i] = hl7Message;
            }

            return messageFlowArray;
        } catch (Exception e) {
            throw new SplitterException("Error splitting the message", e);
        }
    }
}
