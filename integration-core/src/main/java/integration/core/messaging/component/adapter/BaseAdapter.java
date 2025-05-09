package integration.core.messaging.component.adapter;

import java.util.HashMap;
import java.util.Map;

import integration.core.messaging.component.BaseMessagingComponent;

/**
 * Base class for all adapters.  An adapter communications with external entities.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseAdapter extends BaseMessagingComponent {
    private Map<String, String>uriOptions = new HashMap<>();
    
    /**
     * Adds a camel URI options.
     * 
     * @return
     */
    protected void addURIOption(String key, String value) {
        uriOptions.put(key, value);
    }

    
    protected String constructOptions() {
        Class<?> clazz = this.getClass();
        
        while (clazz != null) {
            AdapterOption[] options = clazz.getDeclaredAnnotationsByType(AdapterOption.class);
            for (AdapterOption option : options) {
                addURIOption(option.key(), option.value());
            }
            clazz = clazz.getSuperclass();
        }
        
        
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
