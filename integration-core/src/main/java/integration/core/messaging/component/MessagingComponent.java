package integration.core.messaging.component;

import java.util.Map;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentState;
import integration.core.domain.configuration.ComponentType;
import integration.core.messaging.BaseRoute;

public interface MessagingComponent {
    public String getName();

    public ComponentType getType();
    
    public ComponentCategory getCategory();
    
    public String getComponentPath();
    
    public long getIdentifier();
    
    public void setIdentifier(long identifier);
    
    public BaseRoute getRoute();
    
    public void setRoute(BaseRoute route);
    
    public Map<String, String> getConfiguration();
    
    public void setConfiguration(Map<String, String> configuration);
    
    public ComponentState getInboundState();

    public void setInboundState(ComponentState inboundState);

    public ComponentState getOutboundState();

    public void setOutboundState(ComponentState outboundState);
            
    public String getOwner();
}
