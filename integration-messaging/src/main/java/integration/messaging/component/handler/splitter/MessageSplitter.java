package integration.messaging.component.handler.splitter;

/**
 * Interface for all splitters. A splitter will duplicate a mesage. Each message
 * will be returned as part of the array.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageSplitter {

    public String[] split(String messageBody) throws SplitterException {
        try {
            String[] splitMessages = splitMessage(messageBody);
     //       exchange.getMessage().setHeader("splitCount", splitMessages.length);

            return splitMessages;
        } catch (Exception e) {
            throw new SplitterException("Error splitting the message", e);
        }
    }

    public abstract String[] splitMessage(String messageBody) throws SplitterException;
}
