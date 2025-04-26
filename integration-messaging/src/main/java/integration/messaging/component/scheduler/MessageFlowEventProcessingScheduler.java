//package integration.messaging.component.scheduler;
//
//import java.util.List;
//import java.util.concurrent.locks.Lock;
//
//import org.apache.camel.CamelContext;
//import org.apache.camel.Exchange;
//import org.apache.camel.Processor;
//import org.apache.camel.ProducerTemplate;
//import org.apache.camel.builder.RouteBuilder;
//import org.apache.ignite.Ignite;
//import org.apache.ignite.IgniteCache;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import integration.core.domain.messaging.MessageFlowEventType;
//import integration.core.dto.MessageFlowEventDto;
//import integration.messaging.ComponentIdentifier;
//import integration.messaging.MessageProcessor;
//import integration.messaging.service.MessagingFlowService;
//import jakarta.annotation.PostConstruct;
//
//
///**
// * A Spring scheduler to read an event from the event outbox table and add the message to JMS.
// */
//public abstract class MessageFlowEventProcessingScheduler extends RouteBuilder {
//    private static final Logger logger = LoggerFactory.getLogger(MessageFlowEventProcessingScheduler.class);
//    
//    @Autowired
//    protected MessagingFlowService messagingFlowService;
//
//    @Autowired
//    protected ProducerTemplate producerTemplate;
//    
//    @Autowired
//    protected CamelContext camelContext;
//    
//    @Autowired
//    protected Ignite ignite;
//        
//    public abstract MessageFlowEventType getEventType();
//    
//    public abstract String getJMSDestination();
//
//
//    protected ComponentIdentifier identifier;
//    
//    @Autowired
//    public void setIdentifier(ComponentIdentifier identifier) {
//        this.identifier = identifier;
//    }
//    
//    
//    @PostConstruct
//    public void init() {
//        try {
//            camelContext.addRoutes(this);
//            logger.info("Initialized scheduler route: addToProcessingCompleteTopic-{}", identifier.getComponentPath());
//        } catch (Exception e) {
//            throw new RuntimeException("Error adding route to Camel context", e);
//        }
//    }
//
//    /**
//     * A timer to process messages which have completed inbound message handling.  These message get added to a queue so the components outbound message handler can do further message handling.
//     */
//    @Scheduled(fixedRate = 100, initialDelay = 10000)
//    public void processComponentInboundProcessingCompleteEvents() {
//        if (!camelContext.isStarted()) {
//            return;
//        }
//        
//        logger.info("Scheduler called.  Identifier: " + identifier.getComponentPath());
//
//        IgniteCache<String, Integer> cache = ignite.getOrCreateCache("eventCache3");
//
//        List<MessageFlowEventDto> events = null;
//
//        Lock lock = cache.lock(getEventType() + "-" + identifier.getComponentPath());
//
//        try {
//            // Acquire the lock
//            lock.lock();
//
//            events = messagingFlowService.getEvents(identifier.getComponentRouteId(), 20,getEventType());
//
//            // Each event read we add to the queue and then delete the event.
//            for (MessageFlowEventDto event : events) {
//                long messageFlowId = event.getMessageFlowId();
//
//                logger.info("About to add to JMS");
//                producerTemplate.sendBodyAndHeader("direct:addToJMS-" + identifier.getComponentPath(),event.getId(), MessageProcessor.MESSAGE_FLOW_STEP_ID, messageFlowId);
//            }
//        } finally {
//            // Release the lock
//            lock.unlock();
//        }
//    }
//
//    
//    @Override
//    public void configure() throws Exception {
//        // A route to add the mesage flow step id to a JMS destination.
//        from("direct:addToJMS-" + identifier.getComponentPath())
//            .routeId("addToProcessingCompleteTopic-" + identifier.getComponentPath())
//            .routeGroup(identifier.getComponentPath())
//            .transacted()
//                .process(new Processor() {
//    
//                    @Override
//                    public void process(Exchange exchange) throws Exception {
//                        // Delete the event.
//                        Long eventId = exchange.getMessage().getBody(Long.class);
//                        messagingFlowService.deleteEvent(eventId);
//                        
//                        // Set the message flow step id as the exchange message body so it can be added to the queue.
//                        Long messageFlowId = (Long)exchange.getMessage().getHeader(MessageProcessor.MESSAGE_FLOW_STEP_ID);
//                        exchange.getMessage().setBody(messageFlowId);
//                    }
//                })
//            .to("jms:" + getJMSDestination() + "-" + identifier.getComponentPath());
//    }
//}
