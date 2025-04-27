package integration.messaging.component.adapter;

import java.util.HashMap;
import java.util.Map;

import integration.messaging.component.BaseMessagingComponent;

/**
 * Base class for all adapters.  An adapter communications with external entities.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseAdapter extends BaseMessagingComponent {
    private Map<String, String>uriOptions = new HashMap<>();
    
    public BaseAdapter(String componentName) {
        super(componentName);
        
        setDefaultURIOptions();
    }

    
    /**
     * Adds a camel URI options.
     * 
     * @return
     */
    protected void addURIOption(String key, String value) {
        uriOptions.put(key, value);
    }

    
    /**
     * Sets the default URI options.  The default is none.
     */
    protected void setDefaultURIOptions() {
        
    }

    
    protected String constructOptions() {
        if (uriOptions.isEmpty()) {
            return "";
        }

        StringBuilder uriBuilder = new StringBuilder("?");
        boolean first = true;

        for (Map.Entry<String, String> entry : uriOptions.entrySet()) {
            if (!first) {
                uriBuilder.append("&");
            } else {
                first = false;
            }
            uriBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return uriBuilder.toString();
        
    }
}
