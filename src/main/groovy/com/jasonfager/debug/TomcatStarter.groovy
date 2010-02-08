package com.jasonfager.debug

import com.jasonfager.debug.util.VMStarter;
import com.sun.jdi.VirtualMachine;


class TomcatStarter {
	static final String TOMCAT = "/Users/jason/java_shite/apache-tomcat-5.5.27-eclipse"
	static final String OPTIONS = """\
		-Dcatalina.home="${TOMCAT}"
		-Djava.endorsed.dirs="${TOMCAT}/common/endorsed"
		-Dcatalina.base="${TOMCAT}"
		-Djava.io.tmpdir="${TOMCAT}/temp"
		-Dlog4j.configuration=file:/Users/Jason/java_shite/log4j.properties
		-cp ${TOMCAT}/bin/bootstrap.jar
	"""
	
	static final String MAIN = "org.apache.catalina.startup.Bootstrap"
	
	static VirtualMachine vm() {
		return new VMStarter(OPTIONS, MAIN).start() 
	}
}