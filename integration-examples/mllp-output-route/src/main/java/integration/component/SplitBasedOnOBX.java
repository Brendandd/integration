package integration.component;

import org.springframework.stereotype.Component;

import integration.core.dto.MessageFlowStepDto;
import integration.core.messaging.component.handler.splitter.MessageSplitter;
import integration.core.messaging.component.handler.splitter.SplitterException;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * OBX segment splitter logic.
 */
@Component("splitOnOXBSegments")
public class SplitBasedOnOBX extends MessageSplitter {

    @Override
    public String[] splitMessage(MessageFlowStepDto messageFlowStep) throws SplitterException {
        try {
            HL7Message hl7Message = new HL7Message(messageFlowStep.getMessage().getContent());
            
            int obxCount = hl7Message.getSegmentCount("OBX");

            String[] messageFlowArray = new String[obxCount];

            for (int i = 0; i < obxCount; i++) {
                messageFlowArray[i] = hl7Message.toString();
            }

            return messageFlowArray;
        } catch (Exception e) {
            throw new SplitterException("Error splitting the message", e);
        }
    }
}
