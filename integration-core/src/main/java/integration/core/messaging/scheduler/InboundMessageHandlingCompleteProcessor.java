package integration.core.messaging.scheduler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import integration.core.domain.messaging.MessageFlowEventType;

@Component
public class InboundMessageHandlingCompleteProcessor extends BaseEventProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(InboundMessageHandlingCompleteProcessor.class);

    @Override
    public MessageFlowEventType getEventType() {
        return MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE;
    }

    @Override
    public String getDirectUri() {
        return "addToInboundMessageHandlingCompleteQueue";
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    } 
}
