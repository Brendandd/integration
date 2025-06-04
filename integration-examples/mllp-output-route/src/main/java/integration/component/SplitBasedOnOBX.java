package integration.component;

import org.springframework.stereotype.Component;

import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.type.handler.splitter.MessageSplitter;
import integration.core.runtime.messaging.component.type.handler.splitter.SplitterException;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * OBX segment splitter logic.
 */
@Component("splitOnOXBSegments")
public class SplitBasedOnOBX extends MessageSplitter {

    @Override
    public int splitMessage(MessageFlowDto messageFlow) throws SplitterException {
        try {
            HL7Message hl7Message = new HL7Message(messageFlow.getMessage().getContent());
            
            return hl7Message.getSegmentCount("OBX");
        } catch (Exception e) {
            throw new SplitterException("Error splitting the message", messageFlow.getId(), e);
        }
    }
}
