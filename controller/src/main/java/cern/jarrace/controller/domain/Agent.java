package cern.jarrace.controller.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "agent", cascade = CascadeType.ALL)
    private final List<Service> services = new ArrayList<>();

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

    public List<Service> getServices() {
        return new ArrayList<>(services);
    }

    public void addService(Service service) {
        services.add(service);
        if(service.getAgent() != this) {
            service.setAgent(this);
        }
    }

    public void removeService(Service service) {
        services.remove(service);
    }

    public void clearServices() {
        services.clear();
    }
}
