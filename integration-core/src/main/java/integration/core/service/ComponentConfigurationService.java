package integration.core.service;

import java.util.List;

import integration.core.dto.ComponentDto;
import integration.core.exception.ConfigurationException;

/**
 * @author Brendan Douglas
 *
 */
public interface ComponentConfigurationService {   
    ComponentDto getComponent(long componentId) throws ConfigurationException;
    
    List<ComponentDto> getAllComponents() throws ConfigurationException;
}


