package integration.core.runtime.messaging.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.MessageFlow;

@Repository
public interface MessageFlowRepository extends CrudRepository<MessageFlow, Long> {

}
