package integration.core.domain.configuration;

import java.util.ArrayList;
import java.util.List;

import integration.core.domain.BaseIntegrationDomain;
import integration.core.domain.messaging.MessageFlowStep;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The relationship between a component and a route. A component can be part of
 * multiple routes.
 * 
 * @author Brendan Douglas
 *
 */

@Entity
@Table(name = "component_route")
public class ComponentRoute extends BaseIntegrationDomain {

    private ComponentState inboundState;

    private ComponentState outboundState;

    private List<MessageFlowStep> messageFlows = new ArrayList<>();

    private Component component;

    private Route route;

    private ExternalSystem externalSystem;

    @Column(name = "inbound_state")
    @Enumerated(EnumType.STRING)
    public ComponentState getInboundState() {
        return inboundState;
    }

    public void setInboundState(ComponentState inboundState) {
        this.inboundState = inboundState;
    }

    @Column(name = "outbound_state")
    @Enumerated(EnumType.STRING)
    public ComponentState getOutboundState() {
        return outboundState;
    }

    public void setOutboundState(ComponentState outboundState) {
        this.outboundState = outboundState;
    }

    @OneToMany(mappedBy = "componentRoute", cascade = CascadeType.ALL)
    public List<MessageFlowStep> getMessageFlows() {
        return messageFlows;
    }

    public void setMessageFlows(List<MessageFlowStep> messageFlows) {
        this.messageFlows = messageFlows;
    }

    /**
     * Has the supplied message flow id already been processed by this component.
     * 
     * @param messageFlowId
     * @return
     */
    public boolean hasAlreadyBeenProcessed(long messageFlowId) {
        for (MessageFlowStep step : messageFlows) {
            if (step.getId() == messageFlowId) {
                return true;
            }
        }

        return false;
    }

    @ManyToOne
    @JoinColumn(name = "component_id", nullable = false)
    public Component getComponent() {
        return component;
    }

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    public Route getRoute() {
        return route;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    @ManyToOne
    @JoinColumn(name = "external_system_id", nullable = false)
    public ExternalSystem getExternalSystem() {
        return externalSystem;
    }

    public void setExternalSystem(ExternalSystem externalSystem) {
        this.externalSystem = externalSystem;
    }
}
