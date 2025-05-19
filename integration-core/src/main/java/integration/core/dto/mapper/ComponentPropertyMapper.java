package integration.core.dto.mapper;

import integration.core.domain.configuration.IntegrationComponentProperty;
import integration.core.dto.ComponentPropertyDto;

/**
 * Maps a component property domain object to a component property dto.
 * 
 * @author Brendan Douglas
 */
public class ComponentPropertyMapper extends BaseMapper<ComponentPropertyDto, IntegrationComponentProperty> {

    @Override
    public ComponentPropertyDto doMapping(IntegrationComponentProperty source) {
        ComponentPropertyDto destination = new ComponentPropertyDto();
        
        destination.setId(source.getId());
        destination.setKey(source.getKey());
        destination.setValue(source.getValue());
       
        return destination;
    }
}
