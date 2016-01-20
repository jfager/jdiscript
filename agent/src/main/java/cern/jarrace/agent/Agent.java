package cern.jarrace.agent;

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
    public List<Method> discover(Class<?> clazz);

    /**
     * Executes a specified method using reflexion
     * @param method Method to be executed
     */
    public void run(Method method);



}
