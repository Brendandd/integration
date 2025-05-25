package integration.core.runtime.messaging.component;

import java.util.Map;

import integration.core.domain.configuration.IntegrationComponentCategoryEnum;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;

public interface MessagingComponent {
    public String getName() throws ComponentConfigurationException;

    public IntegrationComponentTypeEnum getType() throws ComponentConfigurationException;
    
    public IntegrationComponentCategoryEnum getCategory() throws ComponentConfigurationException;
    
    public String getComponentPath() throws ComponentConfigurationException, RouteConfigurationException;
    
    public long getIdentifier();
    
    public void setIdentifier(long identifier);
    
    public BaseRoute getRoute();
    
    public void setRoute(BaseRoute route);
    
    public Map<String, String> getConfiguration();
    
    public void setConfiguration(Map<String, String> configuration);
    
    public IntegrationComponentStateEnum getInboundState();

    public void setInboundState(IntegrationComponentStateEnum inboundState);

    public IntegrationComponentStateEnum getOutboundState();

    public void setOutboundState(IntegrationComponentStateEnum outboundState);
                
    public void validateAnnotations() throws ComponentConfigurationException;
}
