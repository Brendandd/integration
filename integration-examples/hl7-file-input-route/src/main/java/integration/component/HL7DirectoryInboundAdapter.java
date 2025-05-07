package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;
import integration.messaging.hl7.component.adapter.directory.BaseHL7InboundDirectoryAdapter;

/**
 * Reads a file from the configured folder.
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "directory-inbound")
public class HL7DirectoryInboundAdapter extends BaseHL7InboundDirectoryAdapter {

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;
    
    public HL7DirectoryInboundAdapter() {
        
        // Add some URI options.
        addURIOption("idempotent", "true");
        addURIOption("idempotentRepository", "#jpaStore");
        addURIOption("move", "processed");
        addURIOption("noop", "false");
    }
    
    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        return messageForwardingPolicy;
    }  
}
