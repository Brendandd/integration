package integration.core.runtime.messaging.component.type.adapter.smb;

import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.adapter.BaseOutboundAdapter;
import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNaming;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import jakarta.annotation.PostConstruct;

/**
 * Base class for all SMB outbound communication points.
 * 
 * @author Brendan Douglas
 *
 */
@ComponentType(type = IntegrationComponentTypeEnum.OUTBOUND_SMB_ADAPTER)
@FileNaming(strategy = "originalFilename") //TODO no need for this anymore.
public abstract class BaseSMBOutboundAdapter extends BaseOutboundAdapter {
        
    @Autowired
    private SMBForwardingProcessor smbForwardingProcessor;
    
    @PostConstruct
    public void BaseSMBOutboundAdapterInit() {
        smbForwardingProcessor.setComponent(this);
    }
    
    
    public String getDestinationFolder() {
        return componentProperties.get("TARGET_FOLDER");
    }

    
    public String getHost() {
        return componentProperties.get("HOST");
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        super.configureRequiredAnnotations();
        
        requiredAnnotations.add(FileNaming.class);
    }

    
    @Override
    protected SMBForwardingProcessor getEgressForwardingProcessor() throws ComponentConfigurationException, RouteConfigurationException {
        return smbForwardingProcessor;
    }
}
