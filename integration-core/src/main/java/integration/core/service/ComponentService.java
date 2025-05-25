package integration.core.service;

import java.util.List;

import integration.core.dto.ComponentDto;
import integration.core.exception.ComponentNotFoundException;
import integration.core.runtime.messaging.exception.retryable.ComponentAccessException;

/**
 * A service for components
 * 
 * @author Brendan Douglas
 *
 */
public interface ComponentService {   
    
    /**
     * Gets a component by id.
     * 
     * @param componentId
     * @return
     * @throws ComponentNotFoundException
     * @throws ComponentAccessException
     */
    ComponentDto getComponent(long componentId) throws ComponentNotFoundException, ComponentAccessException;
    
    
    /**
     * Returns all components.
     * 
     * @return
     * @throws ComponentAccessException
     */
    List<ComponentDto> getAllComponents() throws ComponentAccessException;
}


