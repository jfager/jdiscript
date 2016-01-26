package cern.jarrace.agent;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by jepeders on 1/22/16.
 */
public class AgentRunner {

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("whatever");
        }

        final String agentName = args[0];
        final String entry = args[1];

        try {
            Agent agent = createAgent(agentName);
            agent.run(entry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Agent createAgent(String agentName) throws Exception {
        Class<Agent> clazz = (Class<Agent>) Class.forName(agentName);
        return (Agent) clazz.getConstructor().newInstance();
    }

}
