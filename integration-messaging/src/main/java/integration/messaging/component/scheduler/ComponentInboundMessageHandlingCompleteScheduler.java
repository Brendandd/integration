//package integration.messaging.component.scheduler;
//
//import integration.core.domain.messaging.MessageFlowEventType;
//
///**
// * A scheduler to process component inbound message handling complete events.
// */
//public class ComponentInboundMessageHandlingCompleteScheduler extends MessageFlowEventProcessingScheduler {
//    
//    @Override
//    public MessageFlowEventType getEventType() {
//        return MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE;
//    }
//    
//    
//    @Override
//    public String getJMSDestination() {
//        return "queue:inboundMessageHandlingComplete-" + identifier.getComponentPath();
//    }
//}
