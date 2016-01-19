package cern.jarrace.controller.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Domain class that represents a service provided by an agent
 * @author timartin
 */

@Entity
@Table(name = "SERVICE_TABLE")
public class Service {

    @Id
    @GeneratedValue
    @Column(name = "UUID")
    private Long id;

    @Column(name = "NAME", unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AGENT_ID")
    @JsonIgnore
    private Agent agent;

    // JPA mandatory default constructor
    public Service() {
    }

    public Service(String name) {
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

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
        if(!agent.getServices().contains(this)) {
            agent.addService(this);
        }
    }
}
