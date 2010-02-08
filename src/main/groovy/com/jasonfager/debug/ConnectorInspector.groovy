package com.jasonfager.debug

import com.sun.jdi.Bootstrap
import com.sun.jdi.VirtualMachineManager

public class ConnectorInspector {
	static void main(args) {
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
	}
}