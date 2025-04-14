package integration.core.dto.mapper;

import integration.core.domain.messaging.MessageFlowStep;
import integration.core.dto.MessageFlowStepDto;

/**
 * Maps a message flow step domain object to a message flow step dto.
 * 
 * @author Brendan Douglas
 */
public class MessageFlowStepMapper extends BaseMapper<MessageFlowStepDto, MessageFlowStep> {

    @Override
    public MessageFlowStepDto doMapping(MessageFlowStep source) {
        MessageFlowStepDto destination = new MessageFlowStepDto();

        destination.setId(source.getId());
        destination.setMessageContent(source.getMessage().getContent());
        destination.setMessageFlowId(source.getMessageFlowGroup().getId());

        return destination;
    }
}
