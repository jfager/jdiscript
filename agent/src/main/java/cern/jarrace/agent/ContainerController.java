package cern.jarrace.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * Created by jepeders on 1/20/16.
 */
@RequestMapping("/container")
@RestController
public class ContainerController {

    @Autowired
    AgentContainer agentContainer;

    @RequestMapping("/list")
    public Set<Agent> listAgents() {
        return agentContainer.getAgents().keySet();
    }


}
