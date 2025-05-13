package integration.core.runtime.messaging.component;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.runtime.messaging.BaseRoute;

/**
 * The configuration for a component.
 */
public class ComponentConfiguration {
    protected BaseRoute route;
    protected String owner;
    
    protected IntegrationComponentStateEnum inboundState;
    protected IntegrationComponentStateEnum outboundState;
    
    
    public BaseRoute getRoute() {
        return route;
    }
    
    
    public void setRoute(BaseRoute route) {
        this.route = route;
    }
    
    
    public String getOwner() {
        return owner;
    }
    
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    
    public IntegrationComponentStateEnum getInboundState() {
        return inboundState;
    }
    
    
    public void setInboundState(IntegrationComponentStateEnum inboundState) {
        this.inboundState = inboundState;
    }
    
    
    public IntegrationComponentStateEnum getOutboundState() {
        return outboundState;
    }
    
    
    public void setOutboundState(IntegrationComponentStateEnum outboundState) {
        this.outboundState = outboundState;
    }

    
    public boolean isInboundRunning() {
        return inboundState == IntegrationComponentStateEnum.RUNNING;
    }

    
    public boolean isOutboundRunning() {
        return outboundState == IntegrationComponentStateEnum.RUNNING;
    }

    
    public boolean isInboundStopped() {
        return inboundState == IntegrationComponentStateEnum.STOPPED;
    }

    
    public boolean isOutboundStopped() {
        return outboundState == IntegrationComponentStateEnum.STOPPED;
    }

    
    public void updateConfiguration(ComponentConfiguration newConfiguration) {
        setInboundState(newConfiguration.getInboundState());
        setOutboundState(newConfiguration.getOutboundState());
    }
}
