package cern.jarrace.controller.db.repository;

import cern.jarrace.controller.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interface to be used by spring data to generate repository CRUD operations to
 * manage {@link Agent} domain objects
 * @author timartin
 */
public interface AgentRepository extends JpaRepository <Agent, Long>{
    Agent findByName(String name);
}
