package cern.jarrace.controller.rest.controller;

import cern.jarrace.controller.db.repository.AgentRepository;
import cern.jarrace.controller.db.repository.ServiceRepository;
import cern.jarrace.controller.domain.Agent;
import cern.jarrace.controller.domain.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author timartin
 * Controller that exposes services to manage agents
 */
@RestController
@RequestMapping("/agent")
public class AgentController {

    //TODO add logger
    private static final File DEPLOYMENT_DIR = new File(System.getProperty("java.io.tmpdir"));

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @RequestMapping(value = "/deploy/{agentName}", method = RequestMethod.POST)
    public Agent deploy(@PathVariable("agentName") String agentName, @RequestBody String requestBody,
                         @RequestHeader(name = "testServices") List<String> services){

        String status = "";

        File deploymentFile = DEPLOYMENT_DIR.toPath().resolve(agentName).toFile();

        if(deploymentFile.exists()) {
            if(!deploymentFile.delete()) {
                throw new DeploymentException();
            }
        }

        try {
            deploymentFile.createNewFile();
            FileWriter writer = new FileWriter(deploymentFile);
            writer.write(requestBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            deploymentFile.delete();
            throw new DeploymentException();
        }

        Agent agent = agentRepository.findByName(agentName);

        if(agent != null) {
            status = "REPLACED";
            for (Service service : agent.getServices()) {
                agent.removeService(service);
                serviceRepository.delete(service);
            }
        }else{
            status = "CREATED";
            agent = new Agent(agentName);
        }

        agentRepository.save(agent);
        for(String serviceName : services) {
            Service service = new Service(serviceName);
            agent.addService(service);
            serviceRepository.save(service);
        }

        return agent;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<Agent> list() {
        return agentRepository.findAll();
    }

    @RequestMapping(value = "/service/list", method = RequestMethod.GET)
    public List<Service> listServices() {
        return serviceRepository.findAll();
    }

    @RequestMapping(value = "/{agentName}/service/list", method = RequestMethod.GET)
    public List<Service> listServices(@PathVariable("agentName") String agentName) {
        Agent agent = agentRepository.findByName(agentName);
        return agent.getServices();
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error deploying file")
    private class DeploymentException extends RuntimeException {
    }
}
