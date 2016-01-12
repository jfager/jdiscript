package cern.accsoft.lhc.inspector;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import org.jdiscript.util.VMLauncher;

import cern.accsoft.lhc.inspector.controller.JdiController;
import cern.accsoft.lhc.inspector.gui.Stepper;
import cern.accsoft.lhc.inspector.inspectable.InspectableMethod;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;

/**
 * The main entry point to inspect a Java application.
 */
public class Inspector implements Closeable {

    private final JdiController controller;
    private final Path sourcePath;
    private final Process process;

    private Inspector(JdiController controller, Path sourcePath, Process process) {
        this.controller = controller;
        this.sourcePath = sourcePath;
        this.process = process;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static void main(String[] args) {
        try {
            builder().setBinaryPath(Paths.get("/opt/jepeders/workspace/lhc-inspector/out/production/inspector/"))
                    .setSourcePath(Paths.get("/opt/jepeders/workspace/lhc-inspector/src/main/java/"))
                    .setInspectable("demo.Inspectable", "run").setMainClass("demo.HelloWorld").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            controller.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getCode(String className) throws IOException {
        return Files.readAllLines(Paths.get(sourcePath.toString(), className));
    }

    // public void stepBack() {
    //
    // }

    public InputStream getStandardOut() {
        return process.getInputStream();
    }

    public InputStream getErrorOut() {
        return process.getErrorStream();
    }

    public void stepOver(ThreadReference thread) {
        try {
            thread.resume();
        } catch (Exception e) {
            close();
        }
    }

    public static class Builder {

        private static final String CLASSPATH_PREFIX = "-cp ";

        private String inspectableClassName = null;
        private String inspectableMethodName = null;
        private Path binaryPath = null;
        private Path sourcePath = null;
        private String mainClassName = null;

        public Inspector build() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IOException,
                IllegalConnectorArgumentsException, VMStartException {
            Objects.requireNonNull(inspectableClassName, "Class name not set");
            Objects.requireNonNull(inspectableMethodName, "Method name not set");
            Objects.requireNonNull(binaryPath, "Path to binaries not set");
            Objects.requireNonNull(sourcePath, "Path to sources not set");

            Class<?> inspectableClass = Class.forName(inspectableClassName);
            InspectableMethod method = InspectableMethod.ofClassAndMethod(inspectableClass, inspectableMethodName);

            VMLauncher launcher = new VMLauncher(CLASSPATH_PREFIX + binaryPath.toString(), mainClassName);

            JdiController controller = JdiController.builder().setInspectableListener(Stepper.INSTANCE_LISTENER)
                    .setInspectableMethod(method).setLauncher(launcher).build();
            Inspector inspector = new Inspector(controller, sourcePath, launcher.getProcess());
            Stepper.start(inspector);

            return inspector;
        }

        /**
         * @param path A {@link Path} to the root of the binaries to be inspected.
         */
        public Builder setBinaryPath(Path path) {
            this.binaryPath = path;
            return this;
        }

        /**
         * @param className The full class name of the main class to instrument. Example: org.example.MainClass
         */
        public Builder setMainClass(String className) {
            this.mainClassName = className;
            return this;
        }

        /**
         * @param className The full class name of the class to inspect. Example: org.example.ClassName
         * @param methodName The name of the method to inspect.
         */
        public Builder setInspectable(String className, String methodName) {
            this.inspectableClassName = className;
            this.inspectableMethodName = methodName;
            return this;
        }

        /**
         * @param path A {@link Path} to the root of the source files that are being inspected.
         */
        public Builder setSourcePath(Path path) {
            this.sourcePath = path;
            return this;
        }

    }

}
