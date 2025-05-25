package integration.core.runtime.messaging.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.OutboxEvent;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(name = "getEvents", value = "select e from OutboxEvent e where e.component.id = ?1 AND (e.retryAfter IS NULL OR e.retryAfter <= CURRENT_TIMESTAMP) order by e.createdDate LIMIT ?2")
    public List<OutboxEvent> getEvents(long componentId, int numberToRead);
    
    @Query(name = "getEvents", value = "select e from OutboxEvent e where e.component.id = ?1 AND (e.retryAfter IS NULL OR e.retryAfter <= CURRENT_TIMESTAMP) order by e.createdDate")
    public List<OutboxEvent> getEvents(long componentId);
}
