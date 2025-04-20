package integration.messaging.component.adapter;

import integration.messaging.component.SourceComponent;

/**
 * Base class for all inbound adapters.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseInboundAdapter extends BaseAdapter implements SourceComponent {

    public BaseInboundAdapter(String componentName) {
        super(componentName);
    }

    public abstract String getFromUriString();
}
