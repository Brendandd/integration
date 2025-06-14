package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.annotation.IntegrationComponent;
import integration.messaging.hl7.component.adapter.mllp.inbound.BaseMLLPInboundAdapter;

/**
 * An MLLP inbound adapter.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "MLLP-Inbound-Adapter")
@AllowedContentType(ContentTypeEnum.HL7)
public class MLLPInboundAdapter extends BaseMLLPInboundAdapter {

}
