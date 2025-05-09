package integration.core.messaging.component;

import java.util.Map;

import integration.core.domain.configuration.ComponentCategoryEnum;
import integration.core.domain.configuration.ComponentStateEnum;
import integration.core.domain.configuration.ComponentTypeEnum;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.BaseRoute;

public interface MessagingComponent {
    public String getName() throws ConfigurationException;

    public ComponentTypeEnum getType();
    
    public ComponentCategoryEnum getCategory();
    
    public String getComponentPath() throws ConfigurationException;
    
    public long getIdentifier();
    
    public void setIdentifier(long identifier);
    
    public BaseRoute getRoute();
    
    public void setRoute(BaseRoute route);
    
    public Map<String, String> getConfiguration();
    
    public void setConfiguration(Map<String, String> configuration);
    
    public ComponentStateEnum getInboundState();

    public void setInboundState(ComponentStateEnum inboundState);

    public ComponentStateEnum getOutboundState();

    public void setOutboundState(ComponentStateEnum outboundState);
            
    public String getOwner();
}
