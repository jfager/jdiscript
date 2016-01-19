package cern.jarrace.controller.rest.controller;

import cern.jarrace.controller.db.repository.AgentRepository;
import cern.jarrace.controller.domain.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    @RequestMapping(value = "/deploy/{agentName}", method = RequestMethod.POST)
    public String deploy(@PathVariable("agentName") String agentName, @RequestBody String requestBody){

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
            return "REPLACED";
        }else{
            agentRepository.save(new Agent(agentName));
            return "CREATED";
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<Agent> list() {
        return agentRepository.findAll();
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error deploying file")
    private class DeploymentException extends RuntimeException {
    }
}
