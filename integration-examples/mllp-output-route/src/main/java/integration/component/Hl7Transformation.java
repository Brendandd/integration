package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.annotation.IntegrationComponent;
import integration.core.messaging.component.type.handler.transformation.BaseTransformationProcessingStep;
import integration.core.messaging.component.type.handler.transformation.annotation.UsesTransformer;

@IntegrationComponent(name = "Transform-to-version-2-5")
@UsesTransformer(name = "changeVersionTo2.5")
@AllowedContentType(ContentTypeEnum.HL7)
public class Hl7Transformation extends BaseTransformationProcessingStep {

}
