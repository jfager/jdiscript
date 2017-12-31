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

For more, see the included [examples](src/example/java/org/jdiscript/example).

jdiscript provides

- An event loop that frees you from the details of managing [EventSets](http://download.java.net/jdk8/docs/jdk/api/jpda/jdi/index.html?com/sun/jdi/event/EventSet.html).

- A set of [FunctionalInterfaces](http://download.java.net/jdk8/docs/api/java/lang/FunctionalInterface.html) 
  so you can use lambdas.

- Classes that handle the boilerplate of [launching](src/main/java/org/jdiscript/util/VMLauncher.java) or [attaching to](src/main/java/org/jdiscript/util/VMSocketAttacher.java) a [VirtualMachine](http://download.java.net/jdk8/docs/jdk/api/jpda/jdi/index.html?com/sun/jdi/VirtualMachine.html).

- A [JDIScript class](src/main/java/org/jdiscript/JDIScript.java) that ties it all together and provides convenience methods for common script patterns.

jdiscript was originally focused on providing an API that you would use from languages like 
Groovy, JRuby, or Clojure, under the belief that Java itself was too verbose for a nice 
scripting experience.  With Java 8, this has changed, and straight Java is now compact enough 
that it might not be worth the overhead of switching to another language.  All examples that
ship with jdiscript have been converted to Java 8.   

Note: you need to have the jdk's tools.jar on your classpath in order to use the JDI.
