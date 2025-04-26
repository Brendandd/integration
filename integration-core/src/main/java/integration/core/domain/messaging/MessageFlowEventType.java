package integration.core.domain.messaging;

/**
 * Types of message flow events.
 * 
 * @author Brendan Douglas
 */
public enum MessageFlowEventType {
    COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE,
    MESSAGE_READY_FOR_SENDING, 
    COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE;
}
