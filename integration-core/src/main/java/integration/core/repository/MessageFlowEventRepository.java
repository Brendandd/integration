package integration.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.MessageFlowEvent;

@Repository
public interface MessageFlowEventRepository extends JpaRepository<MessageFlowEvent, Long> {

    @Query(name = "getEvents", value = "select e from MessageFlowEvent e where e.component.id = ?1 AND (e.retryAfter IS NULL OR e.retryAfter <= CURRENT_TIMESTAMP) order by e.createdDate LIMIT ?2")
    public List<MessageFlowEvent> getEvents(long componentId, int numberToRead);
    
    @Query(name = "getEvents", value = "select e from MessageFlowEvent e where e.component.id = ?1 AND (e.retryAfter IS NULL OR e.retryAfter <= CURRENT_TIMESTAMP) order by e.createdDate")
    public List<MessageFlowEvent> getEvents(long componentId);
}
