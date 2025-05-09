package integration.core.messaging.component.adapter.directory;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component("originalFilename")
public class KeepOriginalFileName extends FileNamingStrategy {

    @Override
    protected String getFilename(Exchange exchange, long messageFlowId) {
        String filename = propertyService.getPropertyValue("CamelFileName", messageFlowId);
        
        //TODO If null it is probably an error.
        
        return filename;    
    }
}
