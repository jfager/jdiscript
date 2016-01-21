package cern.jarrace.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class AgentContainer {

    private static final Map<Agent, Map<Class<?>, List<Method>>> agents = new HashMap<>();
    private static int serverPort = 33333;

    public static void main(String[] args) {
//        if (args.length != 2) {
//            throw new IllegalArgumentException("Expected one argument (host[:port]), but received " + args.length);
//        }
//
//        final String name = args[0];
//        final String stringUri = args[1];
//
//        final String controllerEndpoint = "http://" + stringUri;
//        ContainerDiscoverer.discover(agents);
//        registerServices(name, controllerEndpoint);

        ApplicationContext ctx = SpringApplication.run(AgentContainer.class, args);
    }

    @Bean
    public AgentContainer agentContainer() {
        return new AgentContainer();
    }

    @Bean
    public EmbeddedServletContainerFactory getFactory() {
        JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory = new JettyEmbeddedServletContainerFactory();
        jettyEmbeddedServletContainerFactory.setPort(serverPort);
        return jettyEmbeddedServletContainerFactory;
    }

    public Map<Agent, Map<Class<?>, List<Method>>> getAgents() {
        return agents;
    }

    private static void registerServices(String name, String controllerEndpoint) {
        agents.values().stream().forEach(map -> {
            try {
                URL registerUrl = new URL(controllerEndpoint + "/jarrace/container/register/" + name);
                HttpURLConnection connection = (HttpURLConnection) registerUrl.openConnection();
                String response = "{\"port\":"+ serverPort + ",\"host\":\"localhost\"}";
                System.out.println(response);
                connection.getOutputStream().write(response.getBytes());
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
