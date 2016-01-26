package cern.jarrace.agent.impl;

import cern.jarrace.agent.Agent;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by timartin on 20/01/2016.
 */
public class JunitAgent implements Agent {

    @Override
    public void initialize() {
        // Nothing to do here
    }

    @Override
    public List<Method> discover(Class<?> clazz) {
        List<Method> annotatedMethods = new ArrayList<>();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if(method.isAnnotationPresent(Test.class)) {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }

    @Override
    public void run(Object... args) throws IOException {
        String entry = (String) args[0];
        try {
            Class<?> c = Class.forName(entry.substring(0, entry.indexOf(' ')));
            JUnitCore.runClasses(c);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}