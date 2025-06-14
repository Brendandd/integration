package integration.core.runtime.messaging.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import integration.core.domain.messaging.MessageFlow;
import integration.core.domain.messaging.MessageFlowActionType;

@Repository
public interface MessageFlowRepository extends CrudRepository<MessageFlow, Long> {
    
    @Query("SELECT m FROM MessageFlow m WHERE m.component.id = :componentId AND (m.parentMessageFlow is null OR m.parentMessageFlow.id = :parentId) AND m.action = :action")
    Optional<MessageFlow> findExistingAcceptedMessageFlow(@Param("componentId") Long componentId,
                                                          @Param("parentId") Long parentId,
                                                          @Param("action") MessageFlowActionType action);

}
