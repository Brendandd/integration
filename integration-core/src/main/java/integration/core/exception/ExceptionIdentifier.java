package integration.core.exception;

import integration.core.domain.IdentifierType;

/**
 * An identifier type to put into an exception.
 */
public class ExceptionIdentifier {
    private IdentifierType type;
    private Object value;
    
    
    public ExceptionIdentifier(IdentifierType type, Object value) {
        this.type = type;
        this.value = value;
    }

    
    public IdentifierType getType() {
        return type;
    }

    
    public void setType(IdentifierType type) {
        this.type = type;
    }

    
    public Object getValue() {
        return value;
    }

    
    public void setValue(Object value) {
        this.value = value;
    }
}
