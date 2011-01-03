(import org.jdiscript.JDIScript
        org.jdiscript.util.VMSocketAttacher
        (org.jdiscript.handlers OnMonitorContendedEnter
                                OnMonitorContendedEntered
                                OnVMStart)
        com.sun.jdi.event.MonitorContendedEnteredEvent)

(def vm (.attach (new VMSocketAttacher 12345)))
(def j (new JDIScript vm))

(.enable (.monitorContendedEnterRequest j
  (proxy [OnMonitorContendedEnter] []
    (monitorContendedEnter [ev]
      (.printTrace j ev (str "ContendedEnter for " (.monitor ev)))))))

(.enable (.monitorContendedEnteredRequest j
  (proxy [OnMonitorContendedEntered] []
    ; Type hint needed to avoid "Can't call public method of non-public class"
    (monitorContendedEntered [^MonitorContendedEnteredEvent ev]
      (let [timestamp (System/currentTimeMillis)]
        (println (str timestamp ": " (.thread ev)
                    ": ContendedEnter for " (.monitor ev))))))))

(.run j
  (proxy [OnVMStart] []
    (vmStart [ev] (println "Got StartEvent"))))


(println "Shutting Down")