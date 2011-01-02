import org.jdiscript.JDIScript
import org.jdiscript.util.VMSocketAttacher

import org.jdiscript.HandlerConversions._
import com.sun.jdi.event._

object ContentionWatcher {
  def main(args: Array[String]): Unit = {
    val vm = new VMSocketAttacher(12345).attach
    val j = new JDIScript(vm)

    j.monitorContendedEnterRequest((ev: MonitorContendedEnterEvent) => {
      j.printTrace(ev, "ContendedEnter for " + ev.monitor)
    }).enable

    j.monitorContendedEnteredRequest((ev: MonitorContendedEnteredEvent) => {
      val ts = System.currentTimeMillis
      println("%s: %s: ContendedEntered for %s".format(ts, ev.thread, ev.monitor))
    }).enable

    j.run((ev: VMStartEvent) => { println("Got StartEvent") })

    println("Shutting down")
  }
}
