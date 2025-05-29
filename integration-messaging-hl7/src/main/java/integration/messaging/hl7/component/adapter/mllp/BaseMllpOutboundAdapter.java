package integration.messaging.hl7.component.adapter.mllp;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.adapter.BaseOutboundAdapter;
import integration.core.runtime.messaging.component.type.adapter.annotation.AdapterOption;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import jakarta.annotation.PostConstruct;

/**
 * Base class for all MLLP/HL7 Outbound communication points.
 */
@AdapterOption(key = "sync", value = "true")
@AdapterOption(key = "encoders", value = "#hl7encoder")
@AdapterOption(key = "decoders", value = "#hl7decoder")
@AllowedContentType(ContentTypeEnum.HL7)
@ComponentType(type = IntegrationComponentTypeEnum.OUTBOUND_MLLP_ADAPTER)
public abstract class BaseMllpOutboundAdapter extends BaseOutboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMllpOutboundAdapter.class);
    
    @Autowired
    private MllpForwardingProcessor mllpForwardingProcessor;
    
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();

    @Override
    public void addMessageProducer(MessageProducer messageProducer) {
        if (!messageProducers.contains(messageProducer)) {
            this.messageProducers.add(messageProducer);
            messageProducer.addMessageConsumer(this);
        }
    }
    
    
    @PostConstruct
    public void BaseMllpOutboundAdapterInit() {
        mllpForwardingProcessor.setComponent(this);
    }

    
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    public String getTargetHost() {
        return componentProperties.get("TARGET_HOST");
    }

    
    public String getTargetPort() {
        return componentProperties.get("TARGET_PORT");
    }

    
    @Override
    protected MllpForwardingProcessor getEgressForwardingProcessor() throws ComponentConfigurationException, RouteConfigurationException {
        return mllpForwardingProcessor;
    }
}
