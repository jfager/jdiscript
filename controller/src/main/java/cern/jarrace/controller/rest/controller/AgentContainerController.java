package cern.jarrace.controller.rest.controller;

import cern.jarrace.controller.domain.Entrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author timartin
 * Controller that exposes services to manage {@link Entrypoint}s
 */
@RestController
@RequestMapping("/jarrace")
public class AgentContainerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentContainerController.class);
    private static final File DEPLOYMENT_DIR = new File(System.getProperty("java.io.tmpdir"));

    private final Map<String, Set<Entrypoint>> entrypoints = new HashMap<>();

    @RequestMapping(value = "/container/deploy/{name}", method = RequestMethod.POST)
    public void deploy(@PathVariable("name") String name, @RequestBody byte[] jar) throws IOException {
        System.out.println("Deployed " + name);
        String path = writeFile(name, jar);
        entrypoints.put(name, new HashSet<>());
        startContainer(path, name);
    }

    @RequestMapping(value = "/service/register/{name}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registerService(@PathVariable(value = "name") String name, @RequestBody Entrypoint entrypoint){
        System.out.println(name);
        entrypoints.get(name).add(entrypoint);
        LOGGER.info(entrypoints.toString());
    }

    @RequestMapping(value = "/container/list")
    public Set<String> listContainers() {
        return entrypoints.keySet();
    }

    @RequestMapping(value = "/{name}/entrypoint/list")
    public Set<Entrypoint> listServices(@PathVariable(value = "name") String containerName) {
        return entrypoints.get(containerName);
    }

    private void startContainer(String path, String name) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(String.format("%s/bin/java", System.getProperty("java.home")));
        command.add("-cp");
        command.add(path);
        command.add("cern.jarrace.agent.AgentContainer");
        command.add(name);
        command.add("localhost:8080");

        LOGGER.info(String.format("Starting agent container [%s]", command.toString()));
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO().start();
    }

    private String writeFile(String name, byte[] jar) throws IOException {
        File deploymentFile = DEPLOYMENT_DIR.toPath().resolve(name + ".jar").toFile();
        if (deploymentFile.exists()) {
            deploymentFile.delete();
        }
        deploymentFile.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(deploymentFile);
        outputStream.write(jar);
        outputStream.close();
        LOGGER.info("Deployed file + " + deploymentFile.getAbsolutePath());
        return deploymentFile.getAbsolutePath();
    }
}
