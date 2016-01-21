package cern.jarrace.agent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by timartin on 20/01/2016.
 */
public interface Agent {

    public void initialize();

    /**
     * Discover the endpoints
     * @return {@link List} of endpoints
     */
    List<Method> discover(Class<?> clazz);

    /**
     * Executes a specified method using reflexion
     * @param args Method to be executed
     */
    void run(List<String> args, String classPath) throws IOException;
}
