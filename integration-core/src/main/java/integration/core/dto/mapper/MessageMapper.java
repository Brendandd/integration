package integration.core.dto.mapper;

import integration.core.domain.messaging.Message;
import integration.core.dto.MessageDto;

/**
 * Maps a message domain object to a message dto.
 * 
 * @author Brendan Douglas
 */
public class MessageMapper extends BaseMapper<MessageDto, Message> {

    @Override
    public MessageDto doMapping(Message source) {
        MessageDto destination = new MessageDto();
        destination.setId(source.getId());
        destination.setContent(source.getContent());

        return destination;
    }
}
