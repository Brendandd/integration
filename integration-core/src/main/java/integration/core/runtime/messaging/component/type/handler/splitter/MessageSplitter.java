package integration.core.runtime.messaging.component.type.handler.splitter;

import integration.core.dto.MessageFlowDto;

/**
 * Abstract class for all splitters. A splitter will duplicate a message.  A count of the split messages is returned.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageSplitter {

    public int getSplitCount(MessageFlowDto messageFlow) throws SplitterException {
        try {
            return splitMessage(messageFlow);
        } catch (Exception e) {
            throw new SplitterException("Error splitting the message", messageFlow.getId(), e);
        }
    }

    public abstract int splitMessage(MessageFlowDto messageFlow) throws SplitterException;
}
