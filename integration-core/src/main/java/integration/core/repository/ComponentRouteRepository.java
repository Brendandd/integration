package integration.core.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import integration.core.domain.configuration.ComponentRoute;

@Repository
public interface ComponentRouteRepository extends CrudRepository<ComponentRoute, Long> {

    @Query(name = "getByComponentAndRouteId", value = "select cr from ComponentRoute cr where cr.component.id = ?1 and cr.route.id = ?2")
    ComponentRoute getByComponentAndRouteId(long componentId, long routeId);

}
