package integration.core.exception;

/**
 * An identifier type to put into an exception.
 */
public class ExceptionIdentifier {
    private ExceptionIdentifierType type;
    private Object value;
    
    
    public ExceptionIdentifier(ExceptionIdentifierType type, Object value) {
        this.type = type;
        this.value = value;
    }

    
    public ExceptionIdentifierType getType() {
        return type;
    }

    
    public void setType(ExceptionIdentifierType type) {
        this.type = type;
    }

    
    public Object getValue() {
        return value;
    }

    
    public void setValue(Object value) {
        this.value = value;
    }
}
