package integration.core.dto.mapper;

import integration.core.domain.messaging.MessageFlow;
import integration.core.domain.messaging.MessageFlowProperty;
import integration.core.dto.MessageFlowDto;

/**
 * Maps a message flow domain object to a message flow dto.
 * 
 * @author Brendan Douglas
 */
public class MessageFlowMapper extends BaseMapper<MessageFlowDto, MessageFlow> {

    @Override
    public MessageFlowDto doMapping(MessageFlow source) {
        MessageFlowDto destination = new MessageFlowDto();

        MessageMapper messageMapper = new MessageMapper();
        
        destination.setId(source.getId());
        destination.setMessage(messageMapper.doMapping(source.getMessage()));
        
        
        MessageFlowPropertyMapper messageFlowPropertyMapper = new MessageFlowPropertyMapper();
        
        for (MessageFlowProperty property : source.getProperties()) {
            destination.addProperty(messageFlowPropertyMapper.doMapping(property));
        }

        return destination;
    }
}
