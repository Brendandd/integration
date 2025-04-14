package integration.core.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.MessageFlowGroup;

@Repository
public interface MessageFlowRepository extends CrudRepository<MessageFlowGroup, Long> {

}
