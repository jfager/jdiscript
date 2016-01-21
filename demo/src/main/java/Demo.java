import cern.jarrace.agent.AgentContainer;

/**
 * Created by timartin on 20/01/2016.
 */
public class Demo {

    public static void main(String args[]) {
        AgentContainer.main(new String[] {"gradletask", "localhost:8080"});
    }

}
