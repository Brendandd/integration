package integration.core.domain.messaging;

/**
 * The types of message flow status types
 */
public enum MessageFlowActionType {
    
    INGESTED,
    
    MESSAGE_PENDING_FORWARDING,
    MESSAGE_NOT_FORWARDED,
    MESSAGE_FORWARDED,
    
    MESSAGE_ACCEPTED,
    MESSAGE_NOT_ACCEPTED,
    
    // Message accepted or rejected by internal components
    ACCEPTED,
    NOT_ACCEPTED,

    // Message forwarding outcomes by internal components
    FORWARDED,

    // Message created as a result of a split operation
    CREATED_FROM_SPLIT,
    
    // Message transformation
    TRANSFORMED,

    // Acknowledgment sent to the message source
    ACKNOWLEDGMENT_SENT,
    
    TRANSFORMATION_ERROR,
    FILTER_ERROR,
    SPLITTER_ERROR,
    
    PROCESSING_ERROR

}
