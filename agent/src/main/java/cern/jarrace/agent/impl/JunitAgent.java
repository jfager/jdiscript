package cern.jarrace.agent.impl;

import cern.jarrace.agent.Agent;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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
    public void run(String entry, String classPath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder("java -cp ");
        stringBuilder.append(classPath);
        stringBuilder.append(" org.junit.runner.JUnitCore ");
        stringBuilder.append(entry);
        ProcessBuilder processBuilder = new ProcessBuilder(stringBuilder.toString());
        processBuilder.start();
    }
}