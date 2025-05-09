package integration.core.messaging.component.adapter.directory;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.service.MessageFlowPropertyService;

/**
 * 
 */
public abstract class FileNamingStrategy {
    
    @Autowired
    protected MessageFlowPropertyService propertyService;
    
    protected abstract String getFilename(Exchange exchange, long messageFlowId);
    
    

}
