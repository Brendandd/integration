package integration.core.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.Message;

@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {

}
