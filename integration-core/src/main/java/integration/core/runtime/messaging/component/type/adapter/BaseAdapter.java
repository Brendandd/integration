package integration.core.runtime.messaging.component.type.adapter;

import java.util.HashMap;
import java.util.Map;

import integration.core.dto.ComponentDto;
import integration.core.dto.ComponentPropertyDto;
import integration.core.runtime.messaging.component.BaseMessagingComponent;
import integration.core.runtime.messaging.component.type.adapter.annotation.AdapterOption;

/**
 * Base class for all adapters.  An adapter communications with external entities.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseAdapter extends BaseMessagingComponent  {
    private final Map<String, String>uriOptions = new HashMap<>();
       
    /**
     * Adds camel URI options.
     *
     */
    protected void addURIOption(String key, String value) {
        uriOptions.put(key, value);
    }

    
    public String constructAdapterOptions() {
        Class<?> clazz = this.getClass();
        
        while (clazz != null) {
            AdapterOption[] options = clazz.getDeclaredAnnotationsByType(AdapterOption.class);
            for (AdapterOption option : options) {
                addURIOption(option.key(), env.resolvePlaceholders(option.value()));
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

    
    @Override
    public void configure() throws Exception {
        super.configure();
        
        // Timer to update the outbound connection details from the database.
        from("timer://outboundConnectionDetailsChangeTimer-" + getIdentifier() + "?fixedRate=true&period=100&delay=2000")
        .routeId("connectionDetailsChangeTimer-" + getIdentifier())
        .process(exchange -> {
            ComponentDto component = componentConfigurationService.getComponent(identifier);

            for (ComponentPropertyDto property : component.getProperties()) {
                this.componentProperties.put(property.getKey(), property.getValue());
            }
        });
    }
}
