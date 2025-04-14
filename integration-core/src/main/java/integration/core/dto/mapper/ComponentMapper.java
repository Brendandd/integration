package integration.core.dto.mapper;

import integration.core.domain.configuration.Component;
import integration.core.domain.configuration.ComponentProperty;
import integration.core.dto.ComponentDto;

/**
 * Maps a component domain object to a component dto.
 * 
 * @author Brendan Douglas
 */
public class ComponentMapper extends BaseMapper<ComponentDto, Component> {

    @Override
    public ComponentDto doMapping(Component source) {
        ComponentDto destination = new ComponentDto();
        destination.setCategory(source.getCategory());
        destination.setDescription(source.getDescription());
        destination.setId(source.getId());
        destination.setName(source.getName());
        destination.setType(source.getType());

        for (ComponentProperty property : source.getProperties()) {
            destination.addComponentProperty(property.getKey(), property.getValue());
        }

        return destination;
    }
}
