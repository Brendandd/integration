package integration.core.runtime.messaging.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.InboxEvent;

@Repository
public interface InboxEventRepository extends JpaRepository<InboxEvent, Long> {
           
    @Query(value = """
            SELECT * FROM inbox_event e
            WHERE e.component_id = :componentId
              AND (e.id NOT IN (:processedEventIds))
              AND (e.retry_after IS NULL OR e.retry_after <= CURRENT_TIMESTAMP)
            ORDER BY e.created_date
            LIMIT :limit
            """, nativeQuery = true)
     List<InboxEvent> getEventsForComponent(@Param("componentId") long componentId,@Param("processedEventIds") List<Long> processedEventIds, @Param("limit") int limit);
    
    
    @Query(value = """
            SELECT * FROM inbox_event e
            WHERE e.component_id = :componentId
              AND (e.retry_after IS NULL OR e.retry_after <= CURRENT_TIMESTAMP)
            ORDER BY e.created_date
            LIMIT :limit
            """, nativeQuery = true)
     List<InboxEvent> getEventsForComponent(@Param("componentId") long componentId, @Param("limit") int limit);
    
    
    @Query("SELECT m FROM InboxEvent m WHERE m.component.id = :componentId AND jmsMessageId = :jmsMessageId")
    Optional<InboxEvent> findExistingEvent(@Param("componentId") Long componentId,@Param("jmsMessageId") String jmsMessageId);
    
    
    
}
