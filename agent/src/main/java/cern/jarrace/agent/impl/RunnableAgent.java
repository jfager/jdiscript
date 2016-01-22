package cern.jarrace.agent.impl;

import cern.jarrace.agent.Agent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Created by timartin on 22/01/2016.
 */
public class RunnableAgent implements Agent {
    @Override
    public void initialize() {
        // Nothing to do here
    }

    @Override
    public List<Method> discover(Class<?> clazz) {
        if(Runnable.class.isAssignableFrom(clazz)) {
            try {
                return Collections.singletonList(clazz.getMethod("run"));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void run(Object... args) throws IOException {
        String entry = (String) args[0];
        try {
            Class<?> c = Class.forName(entry.substring(0, entry.indexOf(' ')));
            Runnable runnable = (Runnable)c.getConstructor().newInstance();
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
