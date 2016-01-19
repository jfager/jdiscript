package cern.jarrace.controller.db.repository;

import cern.jarrace.controller.domain.Agent;
import cern.jarrace.controller.domain.Service;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interface to be used by spring data to generate repository CRUD operations to
 * manage {@link cern.jarrace.controller.domain.Service} domain objects
 * @author timartin
 */
public interface ServiceRepository extends JpaRepository <Service, Long>{
    Agent findByName(String name);
}
