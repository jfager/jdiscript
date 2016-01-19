package cern.jarrace.controller.domain;

import javax.persistence.*;

/**
 * Domain class that represents an agent deployed to the controller
 * @author timartin
 */
@Entity
@Table(name = "AGENT_TABLE")
public class Agent {

    @Id
    @GeneratedValue
    @Column(name = "UUID")
    private Long id;

    @Column(name = "NAME", unique = true)
    private String name;

    // JPA mandatory default constructor
    public Agent() {
    }

    public Agent(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Agent agent = (Agent) o;

        if (id != null ? !id.equals(agent.id) : agent.id != null) return false;
        return name != null ? name.equals(agent.name) : agent.name == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
