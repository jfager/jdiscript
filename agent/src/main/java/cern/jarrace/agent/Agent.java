package cern.jarrace.agent;

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
    public List<String> discover();

    /**
     * Executes a specified method using reflexion
     * @param method Name of the method to be executed
     */
    public void run(String method);
}
