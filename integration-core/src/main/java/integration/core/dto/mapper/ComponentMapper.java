package integration.core.dto.mapper;

import integration.core.domain.configuration.IntegrationComponent;
import integration.core.dto.ComponentDto;

/**
 * Maps a component domain object to a component dto.
 * 
 * @author Brendan Douglas
 */
public class ComponentMapper extends BaseMapper<ComponentDto, IntegrationComponent> {

    @Override
    public ComponentDto doMapping(IntegrationComponent source) {
        ComponentDto destination = new ComponentDto();
        destination.setId(source.getId());
        destination.setName(source.getName());
        destination.setType(source.getType());
        destination.setCategory(source.getCategory());
        destination.setOwner(source.getOwner());
        destination.setInboundState(source.getInboundState());
        destination.setOutboundState(source.getOutboundState());

        return destination;
    }
}
