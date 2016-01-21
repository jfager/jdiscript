package cern.jarrace.agent;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationObjectDiscoveryListener;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import sun.management.resources.agent;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class AgentContainer {

    private static final Map<Agent, Map<Class<?>, List<Method>>> agents = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one argument (host[:port]), but received " + args.length);
        }

        final String stringUri = args[0];

        final String controllerEndpoint = "http://" + stringUri;
        ContainerDiscoverer.discover(agents);
        //registerServices(controllerEndpoint);

        ApplicationContext ctx = SpringApplication.run(AgentContainer.class, args);
    }

    @Bean
    public AgentContainer agentContainer() {
        return new AgentContainer();
    }

    public Map<Agent, Map<Class<?>, List<Method>>> getAgents() {
        return agents;
    }

    private static void registerServices(String controllerEndpoint) {
        agents.values().stream().forEach(map -> {
            try {
                URL registerUrl = new URL(controllerEndpoint + "/jarrace/container/register/test");
                HttpURLConnection connection = (HttpURLConnection) registerUrl.openConnection();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(connection.getOutputStream(), map);
                System.out.println(connection.getResponseCode());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(
                        String.format("Expected URI in form of host[:port], but received %s: %s", controllerEndpoint, e));
            }  catch (IOException e) {
                throw new RuntimeException(
                    String.format("failed to register container in the controller: %s", e));
            }
        });
    }

}
