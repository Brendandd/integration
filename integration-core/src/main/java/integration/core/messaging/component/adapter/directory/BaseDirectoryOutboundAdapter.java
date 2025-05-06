package integration.core.messaging.component.adapter.directory;

import org.apache.camel.Exchange;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentType;
import integration.core.messaging.component.adapter.BaseOutboundAdapter;

/**
 * Base class for all directory output communication points.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseDirectoryOutboundAdapter extends BaseOutboundAdapter {
    private static final String CAMEL_FILE_NAME = "CamelFileName";

    public String getDestinationFolder() {
        return componentProperties.get("TARGET_FOLDER");
    }
    
    
    @Override
    public String getMessageForwardingUriString() {
        return "file:" + getDestinationFolder() + constructOptions();
    }
    
    
    @Override
    public ComponentType getType() {
        return ComponentType.OUTBOUND_DIRECTORY_ADAPTER;
    }
    
    
    @Override
    public ComponentCategory getCategory() {
        return ComponentCategory.OUTBOUND_ADAPTER;
    }

    
    /**
     * Default implementation which returns the original filename.
     * 
     * @param exchange
     * @param messageFlowId
     * @return
     */
    protected String generateFilename(Exchange exchange, long messageFlowId) {
        String originalFileName = messagingFlowService.retrieveMessageMetaData(CAMEL_FILE_NAME, messageFlowId);
        
        return originalFileName;      
    }

    
    @Override
    protected void preForwardingProcessing(Exchange exchange) {
        // Set the file name as a header.
        Long messageFlowId = (Long)exchange.getMessage().getHeader(MESSAGE_FLOW_STEP_ID);
        String fileName = generateFilename(exchange, messageFlowId);
        exchange.getMessage().setHeader(CAMEL_FILE_NAME, fileName);
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();
    }
}
