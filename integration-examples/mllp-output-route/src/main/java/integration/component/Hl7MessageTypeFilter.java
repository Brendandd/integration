package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.handler.filter.AcceptancePolicy;
import integration.core.messaging.component.handler.filter.BaseFilterProcessingStep;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;

/**
 * HL7 message type filter compoment.
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "Allow-only-ADT-A04")
@AcceptancePolicy(name = "acceptADT^A04")
@ForwardingPolicy(name = "forwardAllMessages")
@AllowedContentType(ContentTypeEnum.HL7)
public class Hl7MessageTypeFilter extends BaseFilterProcessingStep {

}
