package integration.core.runtime.messaging.component;

import java.util.Map;

import integration.core.domain.configuration.IntegrationComponentCategoryEnum;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.exception.AnnotationConfigurationException;
import integration.core.exception.ConfigurationException;
import integration.core.runtime.messaging.BaseRoute;

public interface MessagingComponent {
    public String getName() throws AnnotationConfigurationException;

    public IntegrationComponentTypeEnum getType() throws AnnotationConfigurationException;
    
    public IntegrationComponentCategoryEnum getCategory() throws AnnotationConfigurationException;
    
    public String getComponentPath() throws ConfigurationException, AnnotationConfigurationException;
    
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
                
    public void validateAnnotations() throws AnnotationConfigurationException;
}
