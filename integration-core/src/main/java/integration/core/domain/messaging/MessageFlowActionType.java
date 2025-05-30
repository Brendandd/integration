package integration.core.domain.messaging;

/**
 * The types of message flow status types
 */
public enum MessageFlowActionType {
    
    // Message accepted or rejected by internal components
    ACCEPTED,
    NOT_ACCEPTED,

    // Message forwarding outcomes by internal components
    PENDING_FORWARDING,
    FORWARDED,
    NOT_FORWARDED,

    // Message created as a result of a split operation
    CREATED_FROM_SPLIT,
    
    // Message transformation
    TRANSFORMED,
    NOT_TRANSFORMED,

    // Acknowledgment sent to the message source
    ACKNOWLEDGMENT_SENT,
    
    TRANSFORMATION_ERROR,
    FILTER_ERROR,
    SPLITTER_ERROR,
    
    PROCESSING_ERROR

}
