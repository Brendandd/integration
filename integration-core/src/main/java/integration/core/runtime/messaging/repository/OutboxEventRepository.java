package integration.core.runtime.messaging.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.OutboxEvent;
import integration.core.domain.messaging.OutboxEventType;
import jakarta.persistence.LockModeType;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(name = "getEventsForRoute", value = "select e from OutboxEvent e where e.type in ?3 and (?4 IS NULL OR e.id NOT IN ?4) and e.route.id = ?1 AND (e.retryAfter IS NULL OR e.retryAfter <= CURRENT_TIMESTAMP) order by e.createdDate LIMIT ?2")
    List<OutboxEvent> getEventsForRoute(long routeId, int numberToRead, Set<OutboxEventType>eventTypes, Set<Long>processedEventIds);
       
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(name = "getEventsForComponent", value = "select e from OutboxEvent e where e.type in ?3 and (?4 IS NULL OR e.id NOT IN ?4) and e.component.id = ?1 AND (e.retryAfter IS NULL OR e.retryAfter <= CURRENT_TIMESTAMP) order by e.createdDate LIMIT ?2")
    List<OutboxEvent> getEventsForComponent(long componentId, int numberToRead, Set<OutboxEventType>eventTypes, Set<Long>processedEventIds);
}
