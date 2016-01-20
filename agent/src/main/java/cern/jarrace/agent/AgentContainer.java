package cern.jarrace.agent;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationObjectDiscoveryListener;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class AgentContainer {

    private static final String[] SUPPORTED_ANNOTATIONS = new String[]{"RunWithAgent"};

    private final Map<Agent, Map<Class<?>, List<Method>>> agents = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(AgentContainer.class, args);
    }

    @Bean
    public AgentContainer agentContainer() {
        return new AgentContainer();
    }

    public Map<Agent, Map<Class<?>, List<Method>>> getAgents() {
        return agents;
    }

    public void discover() {
        Discoverer discoverer = new ClasspathDiscoverer();
        discoverer.addAnnotationListener(new ClassAnnotationObjectDiscoveryListener() {
            @Override
            public void discovered(ClassFile clazz, Annotation annotation) {
                RunWithAgent agentAnnotation = (RunWithAgent) annotation;
                Agent agent = agents.keySet().stream()
                        .filter((Agent key) -> key.getClass().equals(agentAnnotation.agent())).findAny().orElseGet(() -> {
                    try {
                        Constructor constructor = agentAnnotation.agent().getConstructor();
                        Agent newAgent = (Agent) constructor.newInstance();
                        newAgent.initialize();
                        agents.put(newAgent, new HashMap<>());
                        return newAgent;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                Map<Class<?>, List<Method>> agentMethods = agents.get(agent);
                try {
                    Class<?> classDefinition = Class.forName(clazz.getName());
                    agentMethods.put(classDefinition, agent.discover(classDefinition));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String[] supportedAnnotations() {
                return SUPPORTED_ANNOTATIONS;
            }
        });
    }

}
