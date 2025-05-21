package integration.core.runtime.messaging.component.type.adapter.smb;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.adapter.BaseOutboundAdapter;
import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNaming;
import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNamingStrategy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;

/**
 * Base class for all SMB outbound communication points.
 * 
 * @author Brendan Douglas
 *
 */
@ComponentType(type = IntegrationComponentTypeEnum.OUTBOUND_SMB_ADAPTER)
@FileNaming(strategy = "originalFilename")
public abstract class BaseSMBOutboundAdapter extends BaseOutboundAdapter {
    private static final String CAMEL_FILE_NAME = "CamelFileName";

    public String getDestinationFolder() {
        return componentProperties.get("TARGET_FOLDER");
    }
    
    
    public String getHost() {
        return componentProperties.get("HOST");
    }
    
    
    @Override
    public String getMessageForwardingUriString(Exchange exchange) {
        return "smb:" + getHost() + "/" + getDestinationFolder() + constructOptions();
    }

    
    /**
     * Returns the filename.
     * 
     * @param exchange
     * @param messageFlowId
     * @return
     * @throws RouteConfigurationException 
     * @throws RetryableException 
     */
    protected String getFilename(Exchange exchange, long messageFlowId) throws MessageFlowProcessingException, ComponentConfigurationException {
        FileNaming annotation = getRequiredAnnotation(FileNaming.class);
                 
        FileNamingStrategy strategy = springContext.getBean(annotation.strategy(), FileNamingStrategy.class);
        return strategy.getFilename(exchange, messageFlowId);
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();
    }

    
    @Override
    protected Map<String, Object>getHeaders(Exchange exchange, long messageFlowId) throws MessageFlowProcessingException, ComponentConfigurationException {
        Map<String, Object> headers = new HashMap<String, Object>();
        
        String fileName = getFilename(exchange, messageFlowId);
        
        if (fileName != null) {
            headers.put(CAMEL_FILE_NAME, fileName);
        }
            
        return headers;
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        super.configureRequiredAnnotations();
        
        requiredAnnotations.add(FileNaming.class);
    }
}
