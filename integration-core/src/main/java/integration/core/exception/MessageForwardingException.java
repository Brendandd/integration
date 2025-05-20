package integration.core.exception;

import java.util.List;

/**
 * An exception which is thrown during when a components forwards a message.
 * 
 * @author Brendan Douglas
 */
public class MessageForwardingException extends IntegrationException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    public MessageForwardingException(String message, List<ExceptionIdentifier>identifiers, Throwable cause) {
        super(message, identifiers, cause, true);
    }
}
