package integration.core.messaging.scheduler;

import java.util.List;
import java.util.concurrent.locks.Lock;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowEventDto;
import integration.core.messaging.component.BaseMessagingComponent;
import integration.core.service.MessagingFlowService;

public abstract class BaseEventProcessor {
    public abstract MessageFlowEventType getEventType();
    
    public abstract String getDirectUri();
    
    @Autowired
    protected CamelContext camelContext;
    
    @Autowired
    protected Ignite ignite;
    
    @Autowired
    private Environment env;
    
    @Autowired
    protected MessagingFlowService messagingFlowService;
    
    @Autowired
    protected ProducerTemplate producerTemplate;
    
    /**
     * A timer to process messages which have completed inbound message handling.  These message get added to a queue so the components outbound message handler can do further message handling.
     */
    @Scheduled(fixedRate = 100, initialDelay = 2000)
    public void processComponentInboundMessageHandlingCompleteEvents() {
        if (!camelContext.isStarted()) {
            return;
        }
        
        String owner = env.getProperty("owner");
        
        IgniteCache<String, Integer> cache = ignite.getOrCreateCache("eventCache3");

        List<MessageFlowEventDto> events = null;

        Lock lock = cache.lock(getEventType()+ "-" + owner);

        try {
            // Acquire the lock
            lock.lock();

            events = messagingFlowService.getEvents(owner,20,getEventType());

            // Each event read we add to the queue and then delete the event.
            for (MessageFlowEventDto event : events) {
                long messageFlowId = event.getMessageFlowId();
                
                String uri = "direct:" + getDirectUri() + "-" + event.getComponentPath();
                getLogger().info("************************BRENDAN READ events: " + uri);
                

                producerTemplate.sendBodyAndHeader(uri, BaseMessagingComponent.MESSAGE_FLOW_STEP_ID, messageFlowId);
            }
        } finally {
            // Release the lock
            lock.unlock();
        }
    }
    
    
    public abstract Logger getLogger();
}
