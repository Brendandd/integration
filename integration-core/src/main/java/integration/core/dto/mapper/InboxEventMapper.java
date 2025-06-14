package integration.core.dto.mapper;

import integration.core.domain.messaging.InboxEvent;
import integration.core.dto.InboxEventDto;

/**
 * Maps a message flow event domain object to a message flow event dto.
 * 
 * @author Brendan Douglas
 */
public class InboxEventMapper extends BaseMapper<InboxEventDto, InboxEvent> {

    @Override
    public InboxEventDto doMapping(InboxEvent source) {
        InboxEventDto destination = new InboxEventDto();
        destination.setMessageFlowId(source.getMessageFlow().getId());
        destination.setId(source.getId());
        destination.setComponentId(source.getComponent().getId());

        return destination;
    }
}
