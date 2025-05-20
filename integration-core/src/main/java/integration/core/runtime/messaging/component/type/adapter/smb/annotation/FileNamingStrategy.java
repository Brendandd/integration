package integration.core.runtime.messaging.component.type.adapter.smb.annotation;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.runtime.messaging.exception.MessageFlowProcessingException;
import integration.core.runtime.messaging.service.MessageFlowPropertyService;

/**
 * 
 */
public abstract class FileNamingStrategy {
    
    @Autowired
    protected MessageFlowPropertyService propertyService;
    
    public abstract String getFilename(Exchange exchange, long messageFlowId) throws MessageFlowProcessingException;
    
    

}
