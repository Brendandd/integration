package integration.core.runtime.messaging.component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Loads the route config.
 */
@Component
@ConditionalOnProperty(name = "component.route-config.enabled", havingValue = "true", matchIfMissing = false)
public class RouteConfigLoader {
    
    @Value("${integration.config.file:/app/route-config.json}")
    private String configFilePath;
    
    private JSONObject configJson;
    
    public JSONObject getConfigJson() {
        return configJson;
    }
    
    
    public Map<String,String> getConfiguration(String routeNameIn, String componentNameIn) {
        JSONArray routes = configJson.getJSONArray("routes");
        
        Map<String, String>configForComponent = new HashMap<String, String>();
        
        for (int i = 0; i < routes.length(); i++) {
            JSONObject route = routes.getJSONObject(i);
            String routeName = route.getString("name");
            
            if (routeName.equals(routeNameIn)) {                
                JSONArray components = route.getJSONArray("components");
                
                for (int j = 0; j < components.length(); j++) {
                    JSONObject component = components.getJSONObject(j);
                    String componentName = component.getString("name");
                    
                    if (componentName.equals(componentNameIn)) {
                        JSONObject properties = component.getJSONObject("properties");
                        
                        for (String key : properties.keySet()) {
                            String value = properties.getString(key);
                            
                            configForComponent.put(key, value);
                        }  
                        
                        break;
                    }
                }
            }
        }
        
        return configForComponent;
    }
    
    
    @PostConstruct
    public void loadConfig() throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(configFilePath)));
        this.configJson = new JSONObject(content);
    }
}
