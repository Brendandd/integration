package integration.core.messaging.component.type.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.type.handler.filter.MessageAcceptancePolicy;
import integration.core.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.messaging.component.type.handler.filter.annotation.AcceptancePolicy;

/**
 * Base class for all outbound adapters.
 * 
 * @author Brendan Douglas
 *
 */
@AcceptancePolicy(name = "acceptAllMessages")
public abstract class BaseOutboundAdapter extends BaseAdapter implements MessageConsumer  {
    protected List<MessageProducer> messageProducers = new ArrayList<>();
    
    @Override
    public void addMessageProducer(MessageProducer messageProducer) {
        if (!messageProducers.contains(messageProducer)) {
            this.messageProducers.add(messageProducer);
            messageProducer.addMessageConsumer(this);
        }
    }
    
    
    @Override
    protected String getBodyContent(MessageFlowDto messageFlowDto) {
        return messageFlowDto.getMessageContent();
    }
    
    
    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() throws ConfigurationException {
        AcceptancePolicy annotation = getRequiredAnnotation(AcceptancePolicy.class);
                       
        return springContext.getBean(annotation.name(), MessageAcceptancePolicy.class);
    }
    

    
    @Override
    public void configure() throws Exception {
        super.configure();

        // Creates one or more routes based on this components source components.  Each route reads from a topic. This is the entry point for outbound route connectors.
        for (MessageProducer messageProducer : messageProducers) {
            
            from("jms:VirtualTopic." + messageProducer.getComponentPath() + "::Consumer." + getComponentPath() + ".VirtualTopic." + messageProducer.getComponentPath() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
                .routeId("inboundEntryPoint-" + messageProducer.getComponentPath() + "-" + messageProducer.getComponentPath())
                .routeGroup(getComponentPath())
                .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
                .transacted()
                    .process(new Processor() {
                    
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // Record the outbound message.
                            Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                            MessageFlowDto parentMessageFlowDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowId);
                                                       
                            MessageFlowPolicyResult result = getMessageAcceptancePolicy().applyPolicy(parentMessageFlowDto);
                            if (result.isSuccess()) {
                                // Record the content received by this component.
                                MessageFlowDto messageFlowDto = messagingFlowService.recordMessageFlow(getIdentifier(), parentMessageFlowId, MessageFlowActionType.ACCEPTED);
                            
                                // Record an event so the message can be forwarded to other components for processing.
                                messagingFlowService.recordMessageFlowEvent(messageFlowDto.getId(),getIdentifier(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE); 
                            } else {
                                messagingFlowService.recordMessageNotAccepted(getIdentifier(), parentMessageFlowId, result, MessageFlowActionType.NOT_ACCEPTED);
                            }
                        }
                    });
        }

        
        // Entry point for a outbound route connectors outbound message handling. 
        from("direct:outboundMessageHandling-" + getIdentifier())
            .routeId("outboundMessageHandling-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())

                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {                       
                        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                        
                        MessageFlowDto forwardedMessageFlowDto = messagingFlowService.recordMessageFlow(getIdentifier(), parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING);
                        messagingFlowService.recordMessageFlowEvent(forwardedMessageFlowDto.getId(), getIdentifier(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                    }
                });
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        requiredAnnotations.add(AcceptancePolicy.class);
    }
}
