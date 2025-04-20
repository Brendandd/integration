package integration.messaging.component.adapter;

import java.util.ArrayList;
import java.util.List;

import integration.messaging.component.DestinationComponent;
import integration.messaging.component.SourceComponent;

/**
 * Base class for all outbound communication points.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseOutboundAdapter extends BaseAdapter implements DestinationComponent {
    public BaseOutboundAdapter(String componentName) {
        super(componentName);
    }

    protected List<String> sourceComponentPaths = new ArrayList<>();

    public abstract String getToUriString();

    @Override
    public void addSourceComponent(SourceComponent sourceComponent) {
        this.sourceComponentPaths.add(sourceComponent.getIdentifier().getComponentPath());
    }
}
