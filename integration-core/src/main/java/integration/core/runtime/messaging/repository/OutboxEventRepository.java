package integration.core.runtime.messaging.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.OutboxEvent;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
           
    @Query("SELECT e FROM OutboxEvent e " +
            "WHERE e.component.id = :componentId " +
            "AND (e.id NOT IN :processedEventIds) " +
            "AND (e.retryAfter IS NULL OR e.retryAfter <= CURRENT_TIMESTAMP) " +
            "ORDER BY e.createdDate")
     List<OutboxEvent> getEventsForComponent(@Param("componentId") long componentId,@Param("processedEventIds") List<Long> processedEventIds,Pageable pageable);
    
    
    @Query("SELECT e FROM OutboxEvent e " +
            "WHERE e.component.id = :componentId " +
            "AND (e.retryAfter IS NULL OR e.retryAfter <= CURRENT_TIMESTAMP) " +
            "ORDER BY e.createdDate")
     List<OutboxEvent> getEventsForComponent(@Param("componentId") long componentId,Pageable pageable);
}
