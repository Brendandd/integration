package integration.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.MessageFlowEvent;
import integration.core.domain.messaging.MessageFlowTypeEvent;

@Repository
public interface MessageFlowEventRepository extends JpaRepository<MessageFlowEvent, Long> {

    @Query(name = "getEvents", value = "select e from MessageFlowEvent e where e.messageFlow.componentRoute.id = ?1 and type = ?2 order by e.createdDate LIMIT ?3")
    public List<MessageFlowEvent> getEvents(long componentRouteId, MessageFlowTypeEvent type, int numberToRead);
}
