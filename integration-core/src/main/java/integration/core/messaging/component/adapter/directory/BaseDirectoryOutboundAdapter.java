package integration.core.messaging.component.adapter.directory;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;

import integration.core.domain.configuration.ComponentTypeEnum;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.component.ComponentType;
import integration.core.messaging.component.adapter.BaseOutboundAdapter;

/**
 * Base class for all directory output communication points.
 * 
 * @author Brendan Douglas
 *
 */
@ComponentType(type = ComponentTypeEnum.OUTBOUND_DIRECTORY_ADAPTER)
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
     */
    protected String getFilename(Exchange exchange, long messageFlowId) {

        FileNaming annotation = this.getClass().getAnnotation(FileNaming.class);
               
        if (annotation == null) {
            throw new ConfigurationException("@FileNaming annotation not found.  It is mandatory for all outbound directory adapters");
        }
        
        FileNamingStrategy strategy = springContext.getBean(annotation.strategy(), FileNamingStrategy.class);
        
        return strategy.getFilename(exchange, messageFlowId);
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();
    }

    
    @Override
    protected Map<String, Object>getHeaders(Exchange exchange, long messageFlowId) {
        Map<String, Object> headers = new HashMap<String, Object>();
        
        String fileName = getFilename(exchange, messageFlowId);
        headers.put(CAMEL_FILE_NAME, fileName);
        
        return headers;
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        super.configureRequiredAnnotations();
        
        requiredAnnotations.add(FileNaming.class);
    }
}
