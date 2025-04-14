package integration.core.domain.messaging;

import org.hibernate.type.TrueFalseConverter;

import integration.core.domain.BaseIntegrationDomain;
import integration.core.domain.configuration.ComponentRoute;
import integration.core.domain.configuration.DirectionEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * A single flow of a message through the internal components.
 * 
 * @author Brendan Douglas
 * 
 */
@Entity
@Table(name = "message_flow_step")
public class MessageFlowStep extends BaseIntegrationDomain {
    private ComponentRoute componentRoute;
    private Message message;
    private MessageFlowStep fromMessageFlowStep;
    private boolean filtered;
    private boolean error;
    private DirectionEnum direction;

    private MessageFlowGroup messageFlowGroup;

    private MessageFlowStepFiltered filteredStep;
    private MessageFlowStepError errorStep;

    public MessageFlowStep() {

    }

    @ManyToOne
    @JoinColumn(name = "component_route_id")
    public ComponentRoute getComponentRoute() {
        return componentRoute;
    }

    public void setComponentRoute(ComponentRoute componentRoute) {
        this.componentRoute = componentRoute;
    }

    @ManyToOne
    @JoinColumn(name = "message_id")
    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @ManyToOne
    @JoinColumn(name = "from_message_flow_step_id")
    public MessageFlowStep getFromMessageFlowStep() {
        return fromMessageFlowStep;
    }

    public void setFromMessageFlowStep(MessageFlowStep fromMessageFlowStep) {
        this.fromMessageFlowStep = fromMessageFlowStep;
    }

    @Convert(converter = TrueFalseConverter.class)
    @Column(name = "filtered")
    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    @Convert(converter = TrueFalseConverter.class)
    @Column(name = "error")
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    @Column(name = "direction")
    @Enumerated(EnumType.STRING)
    public DirectionEnum getDirection() {
        return direction;
    }

    public void setDirection(DirectionEnum direction) {
        this.direction = direction;
    }

    @ManyToOne
    @JoinColumn(name = "message_flow_group_id")
    public MessageFlowGroup getMessageFlowGroup() {
        return messageFlowGroup;
    }

    public void setMessageFlowGroup(MessageFlowGroup messageFlowGroup) {
        this.messageFlowGroup = messageFlowGroup;
    }

    @OneToOne(mappedBy = "messageFlowStep", optional = true, cascade = CascadeType.ALL)
    public MessageFlowStepFiltered getFilteredStep() {
        return filteredStep;
    }

    public void setFilteredStep(MessageFlowStepFiltered filteredStep) {
        this.filteredStep = filteredStep;
    }

    @OneToOne(mappedBy = "messageFlowStep", optional = true, cascade = CascadeType.ALL)
    public MessageFlowStepError getErrorStep() {
        return errorStep;
    }

    public void setErrorStep(MessageFlowStepError errorStep) {
        this.errorStep = errorStep;
    }

    public void filterMessage(String reason, String filterName) {
        this.setFiltered(true);

        MessageFlowStepFiltered filtered = new MessageFlowStepFiltered();
        filtered.setMessageFlowStep(this);
        filtered.setDescription(reason);
        filtered.setFilterName(filterName);

        setFilteredStep(filtered);
    }

    public void errorMessage(String reason) {
        this.setError(true);

        MessageFlowStepError error = new MessageFlowStepError();
        error.setMessageFlowStep(this);
        error.setDescription(reason);

        setErrorStep(error);
    }
}
