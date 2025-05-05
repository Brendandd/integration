package integration.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import integration.core.domain.configuration.IntegrationRoute;

@Repository
public interface RouteRepository extends CrudRepository<IntegrationRoute, Long> {

    @Query(name = "getByName", value = "select r from IntegrationRoute r where r.name = ?1 and r.owner = ?2")
    IntegrationRoute getByName(String name, String owner);

    @Query(name = "getAllRoutes", value = "select r from IntegrationRoute r")
    List<IntegrationRoute> getAllRoutes();
}
