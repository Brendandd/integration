package integration.core.runtime.messaging.component.type.adapter.smb;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNamingStrategy;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;

@Component("originalFilename")
public class KeepOriginalFileName extends FileNamingStrategy {

    @Override
    public String getFilename(Exchange exchange, long messageFlowId) throws MessageFlowProcessingException {
        return (String)propertyService.getPropertyValue("CamelFileName", messageFlowId);
    }
}
