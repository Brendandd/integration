package integration.core.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.MessageFlowStep;

@Repository
public interface MessageFlowStepRepository extends CrudRepository<MessageFlowStep, Long> {

}
