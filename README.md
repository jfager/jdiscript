A scripting frontend for the Java Debugger Interface (JDI).

An example of a real-world use case is available in a post
[introducing jdiscript](http://jasonfager.com/888-introducing-jdiscript/), which finds thread
contention in the [Cassandra](http://cassandra.apache.org/) write path.  At this point Cassandra
itself has moved on, but the jdiscript stuff is still correct.

Right now, jdiscript provides:

- An event-loop/dispatcher that allows you to specify handlers to respond to
  events from particular EventRequests.

- A set of single-method handler interfaces that are easy to implement as
  closures in other languages available on the jvm.

- Utility classes for launching a new VM and handling its process input and
  output, or attaching to an existing VM.

- A top-level script object that allows easy access to frequently used operations.

jdiscript is pure Java, with no dependencies outside of jdk6/7.  Some
sample scripts in other languages are provided to get you started, including
Groovy, JRuby, Clojure, and Scala.  If you run into problems with
your language of choice, or you have ideas for how a common idiom in your
language could be better supported by the underlying Java implementation,
please let me know.

I'd also like to start collecting generally useful scripts into one place, and
creating a comprehensive debugging catalogue that anyone can dip into to help
troubleshoot their code more efficiently.  If you have one you'd like to share,
drop it into a gist and I will link to it from the
[wiki page](http://wiki.github.com/jfager/jdiscript/useful-jdiscripts).
