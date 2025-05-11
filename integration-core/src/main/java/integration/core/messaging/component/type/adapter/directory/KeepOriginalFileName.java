package integration.core.messaging.component.type.adapter.directory;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import integration.core.messaging.MessageFlowException;
import integration.core.messaging.component.type.adapter.directory.annotation.FileNamingStrategy;

@Component("originalFilename")
public class KeepOriginalFileName extends FileNamingStrategy {

    @Override
    public String getFilename(Exchange exchange, long messageFlowId) throws MessageFlowException {
        String filename = propertyService.getPropertyValue("CamelFileName", messageFlowId);
        
        //TODO If null it is probably an error.
        
        return filename;    
    }
}
