package integration.core.service;

import java.util.List;

import integration.core.dto.ComponentDto;
import integration.core.exception.ComponentAccessException;
import integration.core.exception.ComponentNotFoundException;

/**
 * A service for components
 * 
 * @author Brendan Douglas
 *
 */
public interface ComponentService {   
    ComponentDto getComponent(long componentId) throws ComponentNotFoundException, ComponentAccessException;
    
    List<ComponentDto> getAllComponents() throws ComponentAccessException;

    /**
     * Updates a property.  The existing property is end dated.
     * 
     * @param componentId
     * @param propertyId
     */
  //  void StatusChangeResponse updateProperty(long componentId, long propertyId, String newValue);
}


