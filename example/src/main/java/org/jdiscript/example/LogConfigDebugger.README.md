# LogConfigDebugger

Traces how logging gets configured in a running JVM. Answers "where are my log
settings actually coming from?" — especially useful for Spark jobs on Dataproc
where Dataproc, Spark, YARN, and your application all compete to set up logging.

## What It Intercepts

| Framework       | What's Traced                                                  |
|-----------------|----------------------------------------------------------------|
| **Log4j 1.x**   | `PropertyConfigurator.doConfigure`, `DOMConfigurator.doConfigure`, `Category.setLevel/setPriority`, `FileAppender.setFile` |
| **Log4j 2.x**   | `ConfigurationFactory.getConfiguration`, `AbstractConfiguration.start`, `LoggerConfig.setLevel`, `ConfigurationSource` creation |
| **SLF4J**        | `StaticLoggerBinder.getSingleton` (1.x), `LoggerFactory.bind` (2.x), multiple-binding warnings |
| **Logback**      | `ContextInitializer.autoConfig`, `configureByResource`, `Logger.setLevel`, `FileAppender.setFile` |
| **JUL**          | `LogManager.readConfiguration`, `Logger.setLevel` |
| **System props** | Any `System.setProperty` call where the key starts with `log4j`, `logback`, `java.util.logging`, `org.slf4j`, etc. |
| **Classpath**    | `ClassLoader.getResource` calls for `log4j*.properties`, `logback*.xml`, `logging.properties`, etc. |
| **Spark**        | `Logging.initializeLogging`, `Utils.setLogLevel`, Spark config package loading |

## Quick Start

### 1. Start target with JDWP

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
     -jar your-app.jar
```

### 2. Run the debugger

```bash
cd jdiscript
./gradlew :example:build
java --add-modules jdk.jdi \
     -cp example/build/classes/java/main:jdiscript/build/libs/jdiscript-*.jar \
     org.jdiscript.example.LogConfigDebugger 5005
```

## Dataproc / Spark Streaming

### Option A: Debug the driver

```bash
# Submit with JDWP on the driver
gcloud dataproc jobs submit spark \
    --cluster=CLUSTER \
    --class=com.yourco.YourJob \
    --jars=gs://bucket/your-job.jar \
    --properties="spark.driver.extraJavaOptions=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# SSH tunnel to the master node
gcloud compute ssh CLUSTER-m -- -L 5005:localhost:5005

# Attach
java --add-modules jdk.jdi -cp ... org.jdiscript.example.LogConfigDebugger 5005
```

### Option B: Debug an executor

```bash
# Submit with JDWP on executors (pick a port, only one executor per node will bind)
gcloud dataproc jobs submit spark \
    --cluster=CLUSTER \
    --class=com.yourco.YourJob \
    --jars=gs://bucket/your-job.jar \
    --properties="spark.executor.extraJavaOptions=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# SSH tunnel to a worker node
gcloud compute ssh CLUSTER-w-0 -- -L 5005:localhost:5005

# Attach
java --add-modules jdk.jdi -cp ... org.jdiscript.example.LogConfigDebugger 5005
```

### Catching early init

If logging is configured before you can attach, use `suspend=y` so the JVM
waits for the debugger:

```
spark.driver.extraJavaOptions=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005
```

The JVM will freeze at startup until you attach. Attach the debugger, then it
proceeds and you'll see the full configuration timeline from the very beginning.

## Example Output

```
Attaching to localhost:5005 ...
Attached to OpenJDK 64-Bit Server VM (17.0.9+9)
Setting up logging framework interceptors...
Watching: Log4j 1.x, Log4j 2.x, SLF4J, Logback, JUL, Spark, System properties

[2024-03-17T10:23:01.123Z] SYS-PROPERTY      System.setProperty("log4j.configuration", "file:/etc/spark/log4j.properties")
      triggered by: org.apache.spark.deploy.yarn.ApplicationMaster.main(ApplicationMaster.scala:42)
[2024-03-17T10:23:01.456Z] CLASSPATH          URLClassLoader.getResource("log4j.properties")
      triggered by: org.apache.log4j.LogManager.<clinit>(LogManager.java:82)
[2024-03-17T10:23:01.457Z] LOG4J1-CONFIG      PropertyConfigurator.doConfigure(file:/etc/spark/log4j.properties)
      triggered by: org.apache.log4j.LogManager.<clinit>(LogManager.java:95)
[2024-03-17T10:23:01.600Z] SPARK-LOG          Spark Logging.initializeLogging()
      triggered by: org.apache.spark.SparkContext.<init>(SparkContext.scala:310)
[2024-03-17T10:23:01.601Z] LOG4J1-LEVEL       root -> WARN
      triggered by: org.apache.spark.internal.Logging.initializeLogging(Logging.scala:128)
[2024-03-17T10:23:02.100Z] SLF4J-BIND         StaticLoggerBinder.getSingleton() — SLF4J 1.x binding resolved
      triggered by: org.apache.spark.internal.Logging.initializeLogging(Logging.scala:130)

========================================================================
LOG CONFIGURATION TIMELINE (6 events)
========================================================================

--- Chronological Timeline ---

    1. [2024-03-17T10:23:01.123Z] SYS-PROPERTY      System.setProperty("log4j.configuration", ...)
    2. [2024-03-17T10:23:01.456Z] CLASSPATH          URLClassLoader.getResource("log4j.properties")
    3. [2024-03-17T10:23:01.457Z] LOG4J1-CONFIG      PropertyConfigurator.doConfigure(...)
    ...

--- Summary by Category ---

  SYS-PROPERTY       1 event(s)
  CLASSPATH           1 event(s)
  LOG4J1-CONFIG       1 event(s)
  SPARK-LOG           1 event(s)
  LOG4J1-LEVEL        1 event(s)
  SLF4J-BIND          1 event(s)
========================================================================
```
