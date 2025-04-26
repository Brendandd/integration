package integration.messaging.component.handler.transformation;

/**
 * Interface for all transformers.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageTransformer {

    public String transform(String messageBody) throws TransformationException {
        try {
            return transformMessage(messageBody);
        } catch (Exception e) {
            throw new TransformationException("Error transforming the message", e);
        }
    }

    public abstract String transformMessage(String messageBody) throws TransformationException, Exception;
}
