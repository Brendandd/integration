package integration.core.dto;

import java.io.Serializable;

/**
 * Base class for all DTO's
 * 
 * @author Brendan Douglas
 */
public abstract class BaseDto implements Serializable {

    private static final long serialVersionUID = -6379836710128024357L;

    protected long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
