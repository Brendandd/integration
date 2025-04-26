package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.messaging.component.handler.filter.MessageForwardingPolicy;
import integration.messaging.hl7.component.adapter.directory.BaseHL7InboundDirectoryAdapter;

/**
 * Reads a file from the configured folder.
 * 
 * @author Brendan Douglas
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HL7DirectoryInboundAdapter extends BaseHL7InboundDirectoryAdapter {

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;
    
    public HL7DirectoryInboundAdapter() {
        super("directory-inbound");
    }
    
    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        return messageForwardingPolicy;
    }
    
    @Override
    public String getOptions() {
        return "&delete=true";
    }
}
