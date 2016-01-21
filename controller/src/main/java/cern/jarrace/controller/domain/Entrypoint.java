package cern.jarrace.controller.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author timartin
 * Domain class that represents a registered {@link Entrypoint}
 */

@Entity
@Table(name = "ENTRYPOINT_TABLE")
public class Entrypoint {

    /**
     * Unique identifier for the {@link Entrypoint}
     */
    @Id
    @Column(name = "AGENT_NAME", unique = true)
    String agentName;

    /**
     * Port used by the {@link Entrypoint} to expose its services
     */
    @Column(name = "PATH", nullable = false)
    String path;

    // Needed by the JPA specification
    public Entrypoint() {
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}