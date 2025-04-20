package integration.messaging.component.adapter;

import integration.messaging.component.BaseMessagingComponent;

/**
 * Base class for all adapters 
 * 
 * 
 * 
 * @author Brendan Douglas
 */
public abstract class BaseAdapter extends BaseMessagingComponent {

    public BaseAdapter(String componentName) {
        super(componentName);
    }

    public String getOptions() {
        return "";
    }
}
