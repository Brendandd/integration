package integration.messaging.hl7.component.adapter.mllp.inbound;

import static org.apache.camel.component.hl7.HL7.ack;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.IdentifierType;
import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.adapter.annotation.AdapterOption;
import integration.core.runtime.messaging.component.type.adapter.inbound.BaseInboundAdapter;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import jakarta.annotation.PostConstruct;

/**
 * Base class for all MLLP/HL7 inbound adapters.  his components reads the
 * HL7 message, stores it, writes an event and returns an ACK to the sender.
 * 
 * @author Brendan Douglas
 *
 */
@AdapterOption(key = "sync", value = "true")
@AdapterOption(key = "encoders", value = "#hl7encoder")
@AdapterOption(key = "decoders", value = "#hl7decoder")
@AllowedContentType(ContentTypeEnum.HL7)
@ComponentType(type = IntegrationComponentTypeEnum.INBOUND_MLLP_ADAPTER)
public abstract class BaseMLLPInboundAdapter extends BaseInboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMLLPInboundAdapter.class);
    
    @Autowired
    private MLLPInboundAdapterInboxEventProcessor inboxEventProcessor;
    
    @Autowired
    private MLLPInboundAdapterOutboxEventProcessor outboxEventProcessor;
    
    @PostConstruct
    public void BaseMllpInboundAdapterInit() {
        inboxEventProcessor.setComponent(this);
        outboxEventProcessor.setComponent(this);
    }
   
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
    
    
    public String getHost() {
        return "0.0.0.0";
    }
    
    public String getPort() {
        return componentProperties.get("PORT");
    }

    @Override
    public String getFromUriString() {
        String target = getHost() + ":" + getPort();
        return "netty:tcp://" + target +  constructAdapterOptions();
    }
    
    @Override
    public MLLPInboundAdapterInboxEventProcessor getInboxEventProcessor() {
        return inboxEventProcessor;
    }

    
    @Override
    public MLLPInboundAdapterOutboxEventProcessor getOutboxEventProcessor() {
        return outboxEventProcessor;
    }

    
    @Override
    public void configureIngressRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        from(getFromUriString())
        .routeId("ingress-" + getIdentifier())
        .setHeader("contentType", constant(getContentType()))
        .routeGroup(getComponentPath())
        .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
        .transacted("jpaTransactionPolicy")
        
            .process(exchange -> {
                Map<String, Object> headers = exchange.getMessage().getHeaders();
                
                // Store the message received by this inbound adapter.
                String inboundMessageContent = exchange.getMessage().getBody(String.class);
                Long messageFlowId = messageFlowService.recordInitialMessageFlow(inboundMessageContent, getIdentifier(), getContentType(), headers, MessageFlowActionType.INGESTED);
                
                inboxService.recordEvent(messageFlowId, getIdentifier(), getRoute().getIdentifier(), getOwner());
                
                // Set the message flow id as as a header so it can be used later.
                exchange.getMessage().setHeader(IdentifierType.MESSAGE_FLOW_ID.name(), messageFlowId);
            })
            .transform(ack())
            .process(exchange -> {
                long inboundMessageFlowId = (Long)exchange.getMessage().getHeader(IdentifierType.MESSAGE_FLOW_ID.name());
                
                // Store the ACK
                String ackContent = exchange.getMessage().getBody(String.class);
                messageFlowService.recordNewContentMessageFlow(ackContent, getIdentifier(), inboundMessageFlowId, ContentTypeEnum.HL7_ACK, MessageFlowActionType.ACKNOWLEDGMENT_SENT);
            });
    }
}

