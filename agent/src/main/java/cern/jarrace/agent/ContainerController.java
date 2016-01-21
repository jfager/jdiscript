package cern.jarrace.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jepeders on 1/20/16.
 */
@RequestMapping("/container")
@RestController
public class ContainerController {

    @Autowired
    AgentContainer agentContainer;

    @RequestMapping("/list")
    public String listAgents() {
        return agentContainer.getAgents().keySet().stream().map(Agent::toString).collect(Collectors.joining(","));
    }

    @RequestMapping("/{agentName}/list")
    public List<String> listAgentServices(@PathVariable String agentName)  {
        return agentContainer.getAgents()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getClass().getName().equals(agentName))
                .findAny()
                .map(Map.Entry::getValue)
                .map(map -> map.entrySet()
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @RequestMapping("/{agentName}/{agentMethod}/run")
    public String runAgentService(@PathVariable String agentName, @PathVariable String agentMethod) throws IOException {
        Map.Entry<Agent, Map<Class<?>, List<Method>>> agentEntry = agentContainer.getAgents()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getClass().getName().equals(agentName))
                .findAny().orElseThrow(RuntimeException::new);

        String jarClassPath = ContainerController.class.getProtectionDomain().getCodeSource().getLocation().getFile();

        agentEntry.getKey().run(agentMethod, jarClassPath);
        return "Success";
    }
}
