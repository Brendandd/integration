package integration.core.runtime.messaging.component.type.adapter;

import java.util.HashMap;
import java.util.Map;

public abstract class AdapterURIOptions {
    private Map<String, String>uriOptions = new HashMap<>();

    
    /**
     * Adds a camel URI options.
     * 
     * @return
     */
    protected void addURIOption(String key, String value) {
        uriOptions.put(key, value);
    }

    
    protected String getOptionsString() {
       
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
