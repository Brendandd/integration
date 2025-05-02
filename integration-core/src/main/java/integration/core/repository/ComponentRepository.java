package integration.core.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import integration.core.domain.configuration.IntegrationComponent;

@Repository
public interface ComponentRepository extends CrudRepository<IntegrationComponent, Long> {

    @Query(name = "getByName", value = "select c from IntegrationComponent c where c.name = ?1 and c.route.name = ?2 and c.owner = ?3")
    IntegrationComponent getByNameAndRoute(String componentName, String routeName, String owner);

}
