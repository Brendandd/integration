package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.annotation.IntegrationComponent;
import integration.core.messaging.component.type.handler.splitter.BaseSplitterProcessingStep;
import integration.core.messaging.component.type.handler.splitter.annotation.UsesSplitter;

/**
 * A message splitter. Duplicates the message based on the number of OBX
 * segments.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "Split-Based-on-OBX-Segment")
@UsesSplitter(name = "splitOnOXBSegments")
@AllowedContentType(ContentTypeEnum.HL7)
public class Hl7Splitter extends BaseSplitterProcessingStep {

}
