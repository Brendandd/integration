package integration.core.messaging.component.adapter.directory;

import org.apache.camel.Exchange;

import integration.core.domain.configuration.ComponentTypeEnum;
import integration.core.messaging.component.ComponentType;
import integration.core.messaging.component.adapter.BaseOutboundAdapter;

/**
 * Base class for all directory output communication points.
 * 
 * @author Brendan Douglas
 *
 */
@ComponentType(type = ComponentTypeEnum.OUTBOUND_DIRECTORY_ADAPTER)
public abstract class BaseDirectoryOutboundAdapter extends BaseOutboundAdapter {
    private static final String CAMEL_FILE_NAME = "CamelFileName";

    public String getDestinationFolder() {
        return componentProperties.get("TARGET_FOLDER");
    }
    
    
    @Override
    public String getMessageForwardingUriString() {
        return "file:" + getDestinationFolder() + constructOptions();
    }

    
    /**
     * Default implementation which returns the original filename.
     * 
     * @param exchange
     * @param messageFlowId
     * @return
     */
    protected String generateFilename(Exchange exchange, long messageFlowId) {
        String originalFileName = messagingFlowService.getMessageFlowProperty(CAMEL_FILE_NAME, messageFlowId);
        
        return originalFileName;      
    }

    
    @Override
    protected void preForwardingProcessing(Exchange exchange) {
        // Set the file name as a header.
        Long messageFlowId = (Long)exchange.getMessage().getHeader(MESSAGE_FLOW_ID);
        String fileName = generateFilename(exchange, messageFlowId);
        exchange.getMessage().setHeader(CAMEL_FILE_NAME, fileName);
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();
    }
}
