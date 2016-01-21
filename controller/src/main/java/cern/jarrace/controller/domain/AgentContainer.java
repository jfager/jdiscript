package cern.jarrace.controller.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by timartin on 21/01/2016.
 */

@Entity
@Table(name = "CONTAINER_TABLE")
public class AgentContainer {

    @Id
    @Column(name = "NAME", unique = true)
    String name;

    @Column(name = "PORT", nullable = false)
    int port;

    @Column(name = "HOST", nullable = false)
    String host;

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
