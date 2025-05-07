package integration.core.dto.mapper;

import integration.core.domain.messaging.MessageFlowProperty;
import integration.core.dto.MessageFlowPropertyDto;

public class MessageFlowPropertyMapper extends BaseMapper<MessageFlowPropertyDto, MessageFlowProperty> {

    @Override
    public MessageFlowPropertyDto doMapping(MessageFlowProperty source) {
        MessageFlowPropertyDto destination = new MessageFlowPropertyDto();
        
        destination.setKey(source.getKey());
        destination.setValue(source.getValue());
        
        return destination;
    }
}
