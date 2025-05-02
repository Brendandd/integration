package integration.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.MessageFlowEvent;
import integration.core.domain.messaging.MessageFlowEventType;

@Repository
public interface MessageFlowEventRepository extends JpaRepository<MessageFlowEvent, Long> {

    @Query(name = "getEvents", value = "select e from MessageFlowEvent e where e.owner = ?1 and e.type = ?2 order by e.createdDate LIMIT ?3")
    public List<MessageFlowEvent> getEvents(String owner, MessageFlowEventType type, int numberToRead);
}
