package cern.jarrace.gradle;

import cern.jarrace.agent.RunWithAgent;
import cern.jarrace.agent.impl.JunitAgent;
import org.junit.Test;

/**
 * Created by jepeders on 1/19/16.
 */
@RunWithAgent(value = JunitAgent.class)
public class SimpleTest {

    @Test
    public void hello() {
        System.out.println("Hello from number 1");
    }
}
