// Quick script to print out the Connector details for your JVM.
package org.jdiscript

import com.sun.jdi.Bootstrap
import com.sun.jdi.VirtualMachineManager

VirtualMachineManager vmm = Bootstrap.virtualMachineManager()
vmm.allConnectors().each { c ->
    println "${c.name()}:"
    println "  ${c.description()}"
    c.defaultArguments().each { k, v ->
        println "    ${v.name()}"
        println "      label:    ${v.label()}"
        println "      required: ${v.mustSpecify()}"
        println "      default:  ${v.value()}"
        println "      desc:     ${v.description()}"
    }
    println ""
}

println "Default Connector:  ${vmm.defaultConnector().name()}"
