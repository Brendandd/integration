package integration.core.dto.mapper;

import integration.core.domain.messaging.OutboxEvent;
import integration.core.dto.OutboxEventDto;

/**
 * Maps a message flow event domain object to a message flow event dto.
 * 
 * @author Brendan Douglas
 */
public class OutboxEventMapper extends BaseMapper<OutboxEventDto, OutboxEvent> {

    @Override
    public OutboxEventDto doMapping(OutboxEvent source) {
        OutboxEventDto destination = new OutboxEventDto();
        destination.setMessageFlowId(source.getMessageFlow().getId());
        destination.setId(source.getId());
        destination.setComponentId(source.getComponent().getId());

        return destination;
    }
}
