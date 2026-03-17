jdiscript is an elegant wrapper for a more civilized Java Debugger Interface.  It allows you
to write scripts that use the JDI to control and inspect almost anything happening inside a
running JVM.  Think of it as similar to [DTrace](http://dtrace.org/blogs/about/), but with
more Java-specific flexibility and the ability to use a JVM language for scripting.

Here's how you'd print out a stack trace any time a thread tried to enter a
monitor already owned by another thread:

```java
VirtualMachine vm = new VMSocketAttacher(12345).attach();
JDIScript j = new JDIScript(vm);

j.monitorContendedEnterRequest(e -> {
    j.printTrace(e, "ContendedEnter for "+e.monitor());
}).enable();

j.run();
```

For more, see the included [examples](example/src/main/java/org/jdiscript/example).

jdiscript provides

- An event loop that frees you from the details of managing [EventSets](https://docs.oracle.com/en/java/javase/17/docs/api/jdk.jdi/com/sun/jdi/event/EventSet.html).

- A set of [FunctionalInterfaces](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/FunctionalInterface.html)
  so you can use lambdas.

- Classes that handle the boilerplate of [launching](jdiscript/src/main/java/org/jdiscript/util/VMLauncher.java) or [attaching to](jdiscript/src/main/java/org/jdiscript/util/VMSocketAttacher.java) a [VirtualMachine](https://docs.oracle.com/en/java/javase/17/docs/api/jdk.jdi/com/sun/jdi/VirtualMachine.html).

- A [JDIScript class](jdiscript/src/main/java/org/jdiscript/JDIScript.java) that ties it all together and provides convenience methods for common script patterns.

## Requirements

- **Java 17 or later** (JDI is included in the `jdk.jdi` module since Java 9)
- **Gradle 8.x** (wrapper included)

## Building

```bash
./gradlew build
```

To compile the examples:

```bash
./gradlew :example:compileJava
```
