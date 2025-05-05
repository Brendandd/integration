package integration.core.domain.messaging;

/**
 * The types of message flow status types
 */
public enum MessageFlowStepActionType {
    
    // Message routing between routes
    MESSAGE_RECEIVED_FROM_ANOTHER_ROUTE,
    MESSAGE_FORWARDED_TO_ANOTHER_ROUTE,

    // External message receipt
    MESSAGE_RECEIVED_FROM_OUTSIDE_ENGINE,

    // Message accepted or rejected by internal components
    MESSAGE_ACCEPTED,
    MESSAGE_NOT_ACCEPTED,

    // Message forwarding outcomes by internal components
    MESSAGE_FORWARDED,
    MESSAGE_NOT_FORWARDED,

    // Message dispatched to an external system
    MESSAGE_DISPATCHED_TO_OUTSIDE_ENGINE,

    // Message created as a result of a split operation
    CREATED_FROM_SPLIT,

    // Acknowledgment sent to the message source
    ACKNOWLEDGMENT_SENT;
}
