package integration.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import integration.core.domain.configuration.Route;

@Repository
public interface RouteRepository extends CrudRepository<Route, Long> {

    @Query(name = "getByName", value = "select r from Route r where r.name = ?1")
    Route getByName(String name);

    @Query(name = "getAllRoutes", value = "select r from Route r")
    List<Route> getAllRoutes();
}
