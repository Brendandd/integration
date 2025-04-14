package integration.core.dto.mapper;

import integration.core.domain.messaging.MessageFlowEvent;
import integration.core.dto.MessageFlowEventDto;

/**
 * Maps a message flow event domain object to a message flow event dto.
 * 
 * @author Brendan Douglas
 */
public class MessageFlowEventMapper extends BaseMapper<MessageFlowEventDto, MessageFlowEvent> {

    @Override
    public MessageFlowEventDto doMapping(MessageFlowEvent source) {
        MessageFlowEventDto destination = new MessageFlowEventDto();
        destination.setMessageFlowId(source.getMessageFlow().getId());
        destination.setId(source.getId());

        return destination;
    }
}
