package integration.core.runtime.messaging.component.type.adapter.smb;

import java.util.Map;

import org.apache.camel.Exchange;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.adapter.BaseOutboundAdapter;
import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNaming;
import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNamingStrategy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.exception.retryable.MessageForwardingException;

/**
 * Base class for all SMB outbound communication points.
 * 
 * @author Brendan Douglas
 *
 */
@ComponentType(type = IntegrationComponentTypeEnum.OUTBOUND_SMB_ADAPTER)
@FileNaming(strategy = "originalFilename") //TODO no need for this anymore.
public abstract class BaseSMBOutboundAdapter extends BaseOutboundAdapter {
    private static final String CAMEL_FILE_NAME = "CamelFileName";

    public String getDestinationFolder() {
        return componentProperties.get("TARGET_FOLDER");
    }
    
    
    public String getHost() {
        return componentProperties.get("HOST");
    }

    
    private String getMessageForwardingUriString(Exchange exchange) {
        return "smb:" + getHost() + "/" + getDestinationFolder() + constructOptions();
    }
    
    
    @Override
    public void forwardMessage(Exchange exchange, MessageFlowDto messageFlowDto, long eventId) throws MessageForwardingException, ComponentConfigurationException, MessageFlowProcessingException {
     // These can be the original incoming headers or additional properties added.
        Map<String, Object> headers = getHeaders(messageFlowDto);
        
        // Apply the file name strategy.
        String fileName = getFilename(exchange, messageFlowDto.getId());
        
        if (fileName != null) {
            headers.put(CAMEL_FILE_NAME, fileName);
        }
        
        try {
            producerTemplate.sendBodyAndHeaders(getMessageForwardingUriString(exchange), messageFlowDto.getMessageContent(), headers);
        } catch(Exception e) {
            throw new MessageForwardingException("Error forwarding message out of component", eventId, getIdentifier(), messageFlowDto.getId(), e);
        }
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
    protected void configureRequiredAnnotations() {    
        super.configureRequiredAnnotations();
        
        requiredAnnotations.add(FileNaming.class);
    }
}
