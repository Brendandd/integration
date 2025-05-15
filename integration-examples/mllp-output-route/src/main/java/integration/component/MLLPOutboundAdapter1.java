package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.annotation.IntegrationComponent;
import integration.messaging.hl7.component.adapter.mllp.BaseMllpOutboundAdapter;

@IntegrationComponent(name = "MLLP-Outbound-Adapter-1")
@AllowedContentType(ContentTypeEnum.HL7)
public class MLLPOutboundAdapter1 extends BaseMllpOutboundAdapter {

}
