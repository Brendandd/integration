package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.handler.filter.AcceptancePolicy;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;
import integration.core.messaging.component.handler.transformation.BaseTransformationProcessingStep;
import integration.core.messaging.component.handler.transformation.UsesTransformer;

@IntegrationComponent(name = "Transform-to-version-2-5")
@ForwardingPolicy(name = "forwardAllMessages")
@AcceptancePolicy(name = "acceptAllMessages")
@UsesTransformer(name = "changeVersion")
@AllowedContentType(ContentTypeEnum.HL7)
public class Hl7Transformation extends BaseTransformationProcessingStep {

}
