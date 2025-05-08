package integration.component;

import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.handler.filter.AcceptancePolicy;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;
import integration.core.messaging.component.handler.splitter.BaseSplitterProcessingStep;
import integration.core.messaging.component.handler.splitter.UsesSplitter;

/**
 * A message splitter. Duplicates the message based on the number of OBX
 * segments.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "Split-Based-on-OBX-Segment")
@ForwardingPolicy(name = "forwardAllMessages")
@AcceptancePolicy(name = "acceptAllMessages")
@UsesSplitter(name = "splitOnOXBSegments")
public class Hl7Splitter extends BaseSplitterProcessingStep {
    private static final String CONTENT_TYPE = "HL7";

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }
}
