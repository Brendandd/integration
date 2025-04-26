package integration.messaging.component.adapter.directory;

import integration.messaging.component.adapter.BaseOutboundAdapter;

/**
 * Base class for all directory output communication points.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseDirectoryOutboundAdapter extends BaseOutboundAdapter {

    public BaseDirectoryOutboundAdapter(String componentName) {
        super(componentName);
    }

    public String getDestinationFolder() {
        return componentProperties.get("TARGET_FOLDER");
    }

    @Override
    public String getToUriString() {
        return "file:" + getDestinationFolder();
    }
    
    @Override
    public void configure() throws Exception {
        super.configure();
    }
}
