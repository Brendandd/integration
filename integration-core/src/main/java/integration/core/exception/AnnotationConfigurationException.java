package integration.core.exception;

import java.util.List;

/**
 * An exception which is thrown when there is an issue with the route and component annotations.
 * 
 * @author Brendan Douglas
 */
public class AnnotationConfigurationException extends ConfigurationException {
    private static final long serialVersionUID = -8219003265184923387L;
    
    public AnnotationConfigurationException(String message, ExceptionIdentifierType identifierType, long id, String annotationName) {
        super(message, List.of(new ExceptionIdentifier(identifierType, id)), false);
    }
    
    public AnnotationConfigurationException(String message,  ExceptionIdentifierType identifierType, long id) {
        super(message, List.of(new ExceptionIdentifier(identifierType, id)), false);
    }
}
