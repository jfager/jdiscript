require 'java'

java_import org.jdiscript.JDIScript
java_import org.jdiscript.util.VMSocketAttacher

vm = VMSocketAttacher.new(12345).attach
$j = JDIScript.new vm

class OnEnter
  include org.jdiscript.handlers.OnMonitorContendedEnter
  def exec(ev)
    $j.printTrace ev, "ContendedEnter for #{ev.monitor}"
  end
end

$j.monitorContendedEnterRequest(OnEnter.new).enable

#TODO: why doesn't this work?  "can't convert #<Class:xxxxxxxxxxx> into String"
# req = j.monitorContendedEnterRequest {|ev|
#   j.printTrace ev, "ContendedEnter for #{ev.monitor}"
# }.enable

class OnEntered
  include org.jdiscript.handlers.OnMonitorContendedEntered
  def exec(ev)
    timestamp = java.lang.System.currentTimeMillis
    puts "#{timestamp}: #{ev.thread}: ContendedEntered for #{ev.monitor}"
  end
end

$j.monitorContendedEnteredRequest(OnEntered.new).enable

#TODO: why doesn't this work?  "can't convert #<Class:xxxxxxxxxxx> into String"
# req = j.monitorContendedEnteredRequest {|ev|
#   timestamp = java.lang.System.currentTimeMillis
#   puts "#{timestamp}: #{ev.thread}: ContendedEntered for #{ev.monitor}"
# }.enable

class StartNotice
  include org.jdiscript.handlers.OnVMStart
  def exec(event)
    puts "Got StartEvent"
  end
end

#TODO: when passed as block, jruby picks wrong method.
$j.run StartNotice.new

puts "Shutting down"



