package integration.messaging.component.handler.splitter;

import integration.core.dto.MessageFlowStepDto;

/**
 * Interface for all splitters. A splitter will duplicate a mesage. Each message
 * will be returned as part of the array.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageSplitter {

    public String[] split(MessageFlowStepDto messageFlowStep) throws SplitterException {
        try {
            String[] splitMessages = splitMessage(messageFlowStep);

            return splitMessages;
        } catch (Exception e) {
            throw new SplitterException("Error splitting the message", e);
        }
    }

    public abstract String[] splitMessage(MessageFlowStepDto messageFlowStep) throws SplitterException;
}
