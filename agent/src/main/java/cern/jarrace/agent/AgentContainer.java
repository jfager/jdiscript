package cern.jarrace.agent;

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class AgentContainer {

    private static final String[] SUPPORTED_ANNOTATIONS = new String[]{"cern.jarrace.agent.RunWithAgent"};

    private final Map<Agent, Map<Class<?>, List<Method>>> agents = new HashMap<>();

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(AgentContainer.class, args);
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
                try {

                    //ClassMemberValue cmv = (ClassMemberValue)annotation.getMemberValue("value");


                    Class<?> mClazz = Class.forName(clazz.getName());
                    RunWithAgent agentAnnotation = mClazz.getAnnotation(RunWithAgent.class);
                    Agent agent = agents.keySet().stream()
                            .filter((Agent key) -> key.getClass().equals(agentAnnotation.value())).findAny().orElseGet(() -> {
                                Constructor constructor = null;
                                try {
                                    constructor = agentAnnotation.value().getConstructor();
                                    Agent newAgent = (Agent) constructor.newInstance();
                                    newAgent.initialize();
                                    agents.put(newAgent, new HashMap<>());
                                    return newAgent;
                                } catch ( Exception e) {
                                    e.printStackTrace();
                                }

                                return null;
                            });
                    Map<Class<?>, List<Method>> agentMethods = agents.get(agent);
                    try {
                        Class<?> classDefinition = Class.forName(clazz.getName());
                        agentMethods.put(classDefinition, agent.discover(classDefinition));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                }

                @Override
                public String[] supportedAnnotations () {
                    return SUPPORTED_ANNOTATIONS;
                }
            }

            );
            discoverer.discover(true,false,false,false,true,true);
        }

    }
