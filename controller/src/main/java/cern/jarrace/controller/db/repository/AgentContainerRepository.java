package cern.jarrace.controller.db.repository;

import cern.jarrace.controller.domain.AgentContainer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interface to be used by spring data to generate repository CRUD operations to manage {@link AgentContainer}
 * domain objects
 * @author timartin
 */
public interface AgentContainerRepository extends JpaRepository <AgentContainer, String>{
}
