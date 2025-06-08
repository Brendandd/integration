package integration.core.runtime.messaging.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.OutboxEvent;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
           
    @Query(value = """
            SELECT * FROM outbox_event e
            WHERE e.component_id = :componentId
              AND (e.id NOT IN (:processedEventIds))
              AND (e.retry_after IS NULL OR e.retry_after <= CURRENT_TIMESTAMP)
            ORDER BY e.created_date
            LIMIT :limit
            """, nativeQuery = true)
     List<OutboxEvent> getEventsForComponent(@Param("componentId") long componentId,@Param("processedEventIds") List<Long> processedEventIds, @Param("limit") int limit);
    
    
    @Query(value = """
            SELECT * FROM outbox_event e
            WHERE e.component_id = :componentId
              AND (e.retry_after IS NULL OR e.retry_after <= CURRENT_TIMESTAMP)
            ORDER BY e.created_date
            LIMIT :limit
            """, nativeQuery = true)
     List<OutboxEvent> getEventsForComponent(@Param("componentId") long componentId, @Param("limit") int limit);
}
