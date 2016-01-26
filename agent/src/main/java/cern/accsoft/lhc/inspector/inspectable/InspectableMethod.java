/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.accsoft.lhc.inspector.inspectable;

import java.lang.reflect.Method;

import cern.accsoft.lhc.inspector.Inspector;

/**
 * An immutable representation of a method within a class (interfaced or not) that fulfills the criteria for for
 * inspection by an {@link Inspector}. Right now the only criteria is that the method name cannot appear more than once
 * (cannot be overloaded) because JDI has a hard time distinguishing between overloaded methods.
 * 
 * @author jepeders
 */
public class InspectableMethod {

    private final Class<?> inspectableClass;
    private final String methodName;

    private InspectableMethod(Class<?> inspectableClass, String methodName) {
        this.inspectableClass = inspectableClass;
        this.methodName = methodName;
    }

    public Class<?> getInspectableClass() {
        return inspectableClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public static InspectableMethod ofClassAndMethod(Class<?> inspectableClass, String methodName)
            throws SecurityException, NoSuchMethodException {
        verifyNotOverloaded(inspectableClass, methodName);
        return new InspectableMethod(inspectableClass, methodName);
    }

    private static void verifyNotOverloaded(Class<?> classWithMethod, String methodName) throws NoSuchMethodException {
        int methodCount = 0;
        for (Method method : classWithMethod.getMethods()) {
            if (method.getName().equals(methodName)) {
                methodCount++;
            }
        }
        if (methodCount > 1) {
            throw new IllegalArgumentException("Method " + methodName + " cannot be overloaded. Found " + methodCount
                    + " instances.");
        } else if (methodCount < 1) {
            throw new NoSuchMethodException(methodName);
        }
    }
}
