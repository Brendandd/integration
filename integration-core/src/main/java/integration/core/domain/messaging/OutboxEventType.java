package integration.core.domain.messaging;

/**
 * Types of message flow events.
 * 
 * @author Brendan Douglas
 */
public enum OutboxEventType {
    INGRESS_COMPLETE,
    PROCESSING_COMPLETE,
    PENDING_FORWARDING;
}
