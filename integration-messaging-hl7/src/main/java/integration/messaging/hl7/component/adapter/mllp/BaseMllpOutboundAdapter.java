package integration.messaging.hl7.component.adapter.mllp;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.adapter.BaseOutboundAdapter;
import integration.core.runtime.messaging.component.type.adapter.annotation.AdapterOption;

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

    
    public String getTargetHost() {
        return componentProperties.get("TARGET_HOST");
    }

    
    public String getTargetPort() {
        return componentProperties.get("TARGET_PORT");
    }

    
    @Override
    public String getMessageForwardingUriString(Exchange exchange) {
        String target = getTargetHost() + ":" + getTargetPort();
        return "netty:tcp://" + target + constructOptions();
    }
    
    
    @Override
    public void configure() throws Exception {
        super.configure();
    }
}
