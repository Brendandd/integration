package integration.core.runtime.messaging.component.type.adapter.directory;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.exception.ConfigurationException;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.adapter.BaseOutboundAdapter;
import integration.core.runtime.messaging.component.type.adapter.directory.annotation.FileNaming;
import integration.core.runtime.messaging.component.type.adapter.directory.annotation.FileNamingStrategy;
import integration.core.runtime.messaging.exception.MessageFlowException;

/**
 * Base class for all directory output communication points.
 * 
 * @author Brendan Douglas
 *
 */
@ComponentType(type = IntegrationComponentTypeEnum.OUTBOUND_DIRECTORY_ADAPTER)
@FileNaming(strategy = "originalFilename")
public abstract class BaseDirectoryOutboundAdapter extends BaseOutboundAdapter {
    private static final String CAMEL_FILE_NAME = "CamelFileName";

    public String getDestinationFolder() {
        return componentProperties.get("TARGET_FOLDER");
    }
    
    
    @Override
    public String getMessageForwardingUriString(Exchange exchange) {
        return "file:" + getDestinationFolder() + constructOptions();
    }

    
    /**
     * Returns the filename.
     * 
     * @param exchange
     * @param messageFlowId
     * @return
     * @throws ConfigurationException 
     * @throws RetryableException 
     */
    protected String getFilename(Exchange exchange, long messageFlowId) throws MessageFlowException, ConfigurationException {
        FileNaming annotation = getRequiredAnnotation(FileNaming.class);
                 
        FileNamingStrategy strategy = springContext.getBean(annotation.strategy(), FileNamingStrategy.class);
        return strategy.getFilename(exchange, messageFlowId);
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();
    }

    
    @Override
    protected Map<String, Object>getHeaders(Exchange exchange, long messageFlowId) throws MessageFlowException, ConfigurationException {
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
