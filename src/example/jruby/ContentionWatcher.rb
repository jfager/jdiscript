require 'java'

java_import org.jdiscript.JDIScript
java_import org.jdiscript.util.VMSocketAttacher

vm = VMSocketAttacher.new(12345).attach
$j = JDIScript.new vm

req = $j.monitorContendedEnterRequest {|ev|
  $j.printTrace ev, "ContendedEnter for #{ev.monitor}"
}.enable

req = $j.monitorContendedEnteredRequest {|ev|
  timestamp = java.lang.System.currentTimeMillis
  puts "#{timestamp}: #{ev.thread}: ContendedEntered for #{ev.monitor}"
}.enable

class StartNotice
  include org.jdiscript.handlers.OnVMStart
  def exec(event)
    puts "Got StartEvent"
  end
end

#TODO: when passed as block, jruby picks wrong method.
$j.run StartNotice.new

puts "Shutting down"



