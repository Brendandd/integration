package integration.core.exception;

import java.util.List;

/**
 * An exception which is thrown when a message flow is not found
 * 
 * @author Brendan Douglas
 */
public class MessageFlowNotFoundException extends IntegrationException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    private static String MESSAGE = "Message flow not found. Id: ";
    
    public MessageFlowNotFoundException(long messageFlowId) {
        super(MESSAGE + messageFlowId , List.of(new ExceptionIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId)), false);
    }
}
