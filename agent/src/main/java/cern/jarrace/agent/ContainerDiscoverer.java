package cern.jarrace.agent;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationObjectDiscoveryListener;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jepeders on 1/21/16.
 */
public class ContainerDiscoverer {

    private static final String[] SUPPORTED_ANNOTATIONS = new String[]{"cern.jarrace.agent.RunWithAgent"};

    private ContainerDiscoverer() {
        /* Should not be instantiated */
    }

    public static void discover(Map<Agent, Map<Class<?>, List<Method>>> agents) {
        Discoverer discoverer = new ClasspathDiscoverer();
        discoverer.addAnnotationListener(
                new ClassAnnotationObjectDiscoveryListener() {
                     @Override
                     public void discovered(ClassFile clazz, Annotation annotation) {
                         try {

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
