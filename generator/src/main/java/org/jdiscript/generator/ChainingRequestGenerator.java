package org.jdiscript.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;


import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.StringJoiner;

/**
 * Generates wrapper classes for EventRequests that allow for chainable method invocations.
 */
public class ChainingRequestGenerator {

    static final String JDI_REQUEST_PACKAGE = "com.sun.jdi.request";
    static final String JDI_EVENT_REQUEST = JDI_REQUEST_PACKAGE + ".EventRequest";

    static final String REQUESTS_PACKAGE = "org.jdiscript.requests";
    static final String HANDLERS_PACKAGE = "org.jdiscript.handlers";
    static final String EVENTS_PACKAGE = "org.jdiscript.events";

    static final ClassName DEBUG_EVENT_DISPATCHER = ClassName.get(EVENTS_PACKAGE, "DebugEventDispatcher");

    static final String WRAPPED = "wrapped";
    static final String HANDLER = "handler";

    static final File OUT_DIR = new File("./generator/src/generated/java");

    void run() throws IOException {
        try (ScanResult scanResult = scanForJDIRequests()) {
            ClassInfo eventRequestInfo = scanResult.getClassInfo(JDI_EVENT_REQUEST);
            for (ClassInfo classInfo : scanResult.getAllInterfaces()) {
                if (!classInfo.equals(eventRequestInfo) && classInfo.getInterfaces().contains(eventRequestInfo)) {
                    TypeSpec chainingRequestSpec = buildChainingRequest(classInfo.loadClass());
                    writeTypeSpecToDirectory(OUT_DIR, chainingRequestSpec);
                }
            }
        }
    }

    ScanResult scanForJDIRequests() {
        return new ClassGraph()
                .enableSystemJarsAndModules()
                //.verbose()
                .enableAllInfo()
                .whitelistPackages(JDI_REQUEST_PACKAGE)
                .scan();
    }

    TypeSpec buildChainingRequest(Class<?> classInfo) {
        String requestClass = classInfo.getSimpleName();
        String eventClass = requestClass.substring(0, requestClass.lastIndexOf("Request"));
        ClassName eventHandlerClassName = ClassName.get(HANDLERS_PACKAGE, "On" + eventClass);
        ClassName chainingRequestClassName = ClassName.get(REQUESTS_PACKAGE, "Chaining" + requestClass);

        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(chainingRequestClassName)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Generated chainable wrapper class for {@link $L}\n", classInfo.getCanonicalName())
                .addField(classInfo, WRAPPED, Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(classInfo, WRAPPED)
                        .addStatement("this.$N = $N", WRAPPED, WRAPPED)
                        .build())
                .addMethod(MethodSpec.methodBuilder("addHandler")
                        .returns(chainingRequestClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(eventHandlerClassName, HANDLER)
                        .beginControlFlow("if ($N != null)", HANDLER)
                        .addStatement("$T.addHandler($N, $N)", DEBUG_EVENT_DISPATCHER, WRAPPED, HANDLER)
                        .endControlFlow()
                        .addStatement("return this")
                        .build());

        for (Method method : classInfo.getMethods()) {
            typeSpec.addMethod(buildMethodSpec(method, chainingRequestClassName));
        }

        return typeSpec.build();
    }

    // If the method is void, return the ChainingRequest instead.
    // Otherwise, just delegate to the method.
    MethodSpec buildMethodSpec(Method method, ClassName chainingRequestClassName) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(method.getName())
                .addModifiers(Modifier.PUBLIC);

        StringJoiner callParams = new StringJoiner(", ", "(", ")");
        StringJoiner javaDocParams = new StringJoiner(", ", "(", ")");
        for (Parameter param: method.getParameters()) {
            methodSpec.addParameter(param.getParameterizedType(), param.getName());
            callParams.add(param.getName());
            String typeName = param.getParameterizedType().getTypeName();
            javaDocParams.add(typeName);
        }

        methodSpec.addJavadoc("@see $T#$N$L\n", method.getDeclaringClass(), method.getName(), javaDocParams.toString());

        for (Parameter param: method.getParameters()) {
            methodSpec.addJavadoc("@param $L Unable to match param name, positionally same as wrapped class.\n", param.getName());
        }

        Class<?> returnType = method.getReturnType();
        if (void.class.equals(returnType)) {
            methodSpec.returns(chainingRequestClassName)
                    .addJavadoc("@return This $T for chainable method calls\n", chainingRequestClassName)
                    .addStatement("$N.$N$L", WRAPPED, method.getName(), callParams.toString())
                    .addStatement("return this");

        } else {
            methodSpec.returns(returnType)
                    .addJavadoc("@return The $T returned by the wrapped $T\n", returnType, method.getDeclaringClass())
                    .addStatement("return $N.$N$L", WRAPPED, method.getName(), callParams.toString());
        }

        return methodSpec.build();
    }

    void writeTypeSpecToDirectory(File outDir, TypeSpec typeSpec) throws IOException {
        JavaFile javaFile = JavaFile.builder(REQUESTS_PACKAGE, typeSpec).build();
        javaFile.writeTo(outDir);
    }

    public static void main(String[] args) {
        ChainingRequestGenerator generator = new ChainingRequestGenerator();
        try {
            generator.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
