package integration.messaging.hl7.component.adapter.mllp;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentType;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.adapter.BaseOutboundAdapter;

/**
 * Base class for all MLLP/HL7 Outbound communication points.
 */
public abstract class BaseMllpOutboundAdapter extends BaseOutboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMllpOutboundAdapter.class);
    
    private static final String CONTENT_TYPE = "HL7";
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();

    @Override
    public void addMessageProducer(MessageProducer messageProducer) {
        if (!messageProducers.contains(messageProducer)) {
            this.messageProducers.add(messageProducer);
            messageProducer.addMessageConsumer(this);
        }
    }
    
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.OUTBOUND_MLLP_ADAPTER;
    }

    @Override
    public ComponentCategory getCategory() {
        return ComponentCategory.OUTBOUND_ADAPTER;
    }

    
    @Override
    protected void setDefaultURIOptions() {
        addURIOption("sync", "true");
        addURIOption("encoders", "#hl7encoder");
        addURIOption("decoders", "#hl7decoder");  
    }

    
    public String getTargetHost() {
        return componentProperties.get("TARGET_HOST");
    }

    
    public String getTargetPort() {
        return componentProperties.get("TARGET_PORT");
    }

    
    @Override
    public String getMessageForwardingUriString() {
        String target = getTargetHost() + ":" + getTargetPort();
        return "netty:tcp://" + target + constructOptions();
    }

    
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }
    
    
    @Override
    public void configure() throws Exception {
        super.configure();
    }
}
