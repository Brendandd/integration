package integration.core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

/**
 * A collection of utilities.
 * 
 * @author Brendan Douglas
 */
public class Utils {
    public static String convertToJSON(Map<String, Object> map) {
        return new JSONObject(map).toString();
    }

    public static Map<String, Object> convertFromJSON(String json) {
        JSONObject data = new JSONObject(json);
        Map<String, Object> map = new HashMap<>();

        Iterator<?> it = data.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            map.put(key, data.get(key));
        }

        return map;
    }
}
