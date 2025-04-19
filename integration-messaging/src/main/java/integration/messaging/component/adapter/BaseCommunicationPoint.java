package integration.messaging.component.adapter;

import integration.messaging.component.BaseMessagingComponent;

/**
 * Base class for all communication points. 
 * 
 * 
 * 
 * @author Brendan Douglas
 */
public abstract class BaseCommunicationPoint extends BaseMessagingComponent {

    public BaseCommunicationPoint(String componentName) {
        super(componentName);
    }

    public String getOptions() {
        return "";
    }
}
