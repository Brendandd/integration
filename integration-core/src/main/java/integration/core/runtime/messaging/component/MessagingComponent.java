package integration.core.runtime.messaging.component;

import java.util.Map;

import integration.core.domain.configuration.IntegrationComponentCategoryEnum;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;

public interface MessagingComponent {
    String getName() throws ComponentConfigurationException;

    IntegrationComponentTypeEnum getType() throws ComponentConfigurationException;

    IntegrationComponentCategoryEnum getCategory() throws ComponentConfigurationException;

    String getComponentPath() throws ComponentConfigurationException, RouteConfigurationException;

    long getIdentifier();

    void setIdentifier(long identifier);

    BaseRoute getRoute();

    void setRoute(BaseRoute route);

    Map<String, String> getConfiguration();

    void setConfiguration(Map<String, String> configuration);

    IntegrationComponentStateEnum getInboundState();

    void setInboundState(IntegrationComponentStateEnum inboundState);

    IntegrationComponentStateEnum getOutboundState();

    void setOutboundState(IntegrationComponentStateEnum outboundState);

    void validateAnnotations() throws ComponentConfigurationException;
}
