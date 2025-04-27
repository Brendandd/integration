package integration.core.dto.mapper;

import integration.core.domain.messaging.MessageMetaData;
import integration.core.dto.MessageMetaDataDto;

public class MessageMetaDataMapper extends BaseMapper<MessageMetaDataDto, MessageMetaData> {

    @Override
    public MessageMetaDataDto doMapping(MessageMetaData source) {
        MessageMetaDataDto destination = new MessageMetaDataDto();
        
        destination.setKey(source.getKey());
        destination.setValue(source.getValue());
        
        return destination;
    }
}
