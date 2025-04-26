package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import integration.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.messaging.hl7.component.adapter.directory.BaseHL7OutboundDirectoryAdapter;

/**
 * Component to write the file.
 * 
 * @author Brendan Douglas
 * 
 *         TODO filename not retained in all situations.
 * 
 */
@Component
public class HL7DirectoryOutboundAdapter extends BaseHL7OutboundDirectoryAdapter {

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;

    public HL7DirectoryOutboundAdapter() {
        super("directory-outbound");
    }

    private static final String CONTENT_TYPE = "HL7";

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }
    
    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() {
        return messageAcceptancePolicy;
    }
}
