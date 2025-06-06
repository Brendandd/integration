package integration.messaging.hl7.component.adapter.mllp;

import static org.apache.camel.component.hl7.HL7.ack;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.IdentifierType;
import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.adapter.BaseInboundAdapter;
import integration.core.runtime.messaging.component.type.adapter.annotation.AdapterOption;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;

/**
 * Base class for all MLLP/HL7 inbound communication points. This components reads the
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
public abstract class BaseMllpInboundAdapter extends BaseInboundAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMllpInboundAdapter.class);
   
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
    public void configureIngressRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        from(getFromUriString())
        .routeId("ingress-" + getIdentifier())
        .setHeader("contentType", constant(getContentType()))
        .routeGroup(getComponentPath())
        .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
        .transacted()
        
            .process(exchange -> {
                Map<String, Object> headers = exchange.getMessage().getHeaders();
                
                // Store the message received by this inbound adapter.
                String inboundMessageContent = exchange.getMessage().getBody(String.class);
                MessageFlowDto inboundMessageFlowDto = messageFlowService.recordInitialMessageFlow(inboundMessageContent, getIdentifier(), getContentType(), headers, MessageFlowActionType.ACCEPTED);
                
                // Set the message flow id as as a header so it can be used later.
                exchange.getMessage().setHeader(IdentifierType.MESSAGE_FLOW_ID.name(), inboundMessageFlowDto.getId());
            })
            .transform(ack())
            .process(exchange -> {
                long inboundMessageFlowId = (Long)exchange.getMessage().getHeader(IdentifierType.MESSAGE_FLOW_ID.name());
                
                // Store the ACK
                String ackContent = exchange.getMessage().getBody(String.class);
                messageFlowService.recordNewContentMessageFlow(ackContent, getIdentifier(), inboundMessageFlowId, ContentTypeEnum.HL7_ACK, MessageFlowActionType.ACKNOWLEDGMENT_SENT);
                                                       
                // Final step in the ingress route is to write an event for the outbox.
                outboxService.recordEvent(inboundMessageFlowId,getIdentifier(), getRoute().getIdentifier(), getOwner(), OutboxEventType.INGRESS_COMPLETE); 
            });
        
    }
}

