package cern.jarrace.controller.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author timartin
 * Domain class that represents a registered {@link AgentContainer}
 */

@Entity
@Table(name = "CONTAINER_TABLE")
public class AgentContainer {

    /**
     * Unique identifier for the {@link AgentContainer}
     */
    @Id
    @Column(name = "NAME", unique = true)
    String name;

    /**
     * Port used by the {@link AgentContainer} to expose its services
     */
    @Column(name = "PORT", nullable = false)
    int port;

    /**
     * Host where the {@link AgentContainer} is running
     */
    @Column(name = "HOST", nullable = false)
    String host;

    // Needed by the JPA specification
    public AgentContainer() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
