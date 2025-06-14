package integration.core.runtime.messaging.component.type.adapter.smb.outbound;

import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.adapter.outbound.BaseOutboundAdapter;
import integration.core.runtime.messaging.component.type.adapter.outbound.BaseOutboundAdapterInboxEventProcessor;
import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNaming;
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
    private SMBOutboundAdapterOutboxEventProcessor outboxEventProcessor;
    
    @Autowired
    private SMBOutboundAdapterInboxEventProcessor inboxEventProcessor;
    
    @PostConstruct
    public void BaseSMBOutboundAdapterInit() {
        outboxEventProcessor.setComponent(this);
        inboxEventProcessor.setComponent(this);
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
    public SMBOutboundAdapterOutboxEventProcessor getOutboxEventProcessor() {
        return outboxEventProcessor;
    }
    
    
    @Override
    public BaseOutboundAdapterInboxEventProcessor getInboxEventProcessor() {
        return inboxEventProcessor;
    }
}
