package integration.core.messaging.component;

import java.util.Map;

import integration.core.domain.configuration.ComponentCategory;
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
    
    public void setInboundRunning(boolean isRunning);
    
    public void setOutboundRunning(boolean isRunning);
    
    public boolean isInboundRunning();
    
    public boolean isOutboundRunning();
        
    public String getOwner();
}
