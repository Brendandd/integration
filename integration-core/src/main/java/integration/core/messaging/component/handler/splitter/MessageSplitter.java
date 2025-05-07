package integration.core.messaging.component.handler.splitter;

import integration.core.dto.MessageFlowDto;

/**
 * Interface for all splitters. A splitter will duplicate a mesage. Each message
 * will be returned as part of the array.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageSplitter {

    public String[] split(MessageFlowDto messageFlow) throws SplitterException {
        try {
            String[] splitMessages = splitMessage(messageFlow);

            return splitMessages;
        } catch (Exception e) {
            throw new SplitterException("Error splitting the message", e);
        }
    }

    public abstract String[] splitMessage(MessageFlowDto messageFlow) throws SplitterException;
}
