package integration.messaging.component.adapter;

import integration.messaging.component.BaseMessagingComponent;

/**
 * Base class for all adapters.  An adapter communications with external entities.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseAdapter extends BaseMessagingComponent {
    
    public BaseAdapter(String componentName) {
        super(componentName);
    }
    
    /**
     * Returns the Camel component options.  Implementations can provide the required options if required.
     * 
     * @return
     */
    public String getOptions() {
        return "";
    }
}
