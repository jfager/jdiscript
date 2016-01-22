package cern.jarrace.gradle;

import cern.jarrace.agent.RunWithAgent;
import cern.jarrace.agent.impl.RunnableAgent;

/**
 * Created by timartin on 22/01/2016.
 */

@RunWithAgent(RunnableAgent.class)
public class SimpleRunnable implements Runnable{
    @Override
    public void run() {
        System.out.println("Hello from the other side [AGAIN]");
    }
}
