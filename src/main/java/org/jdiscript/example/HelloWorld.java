package org.jdiscript.example;

import java.util.HashMap;
import java.util.Map;

//A test class you can use for simple debugging scripts.
public class HelloWorld {
	private String helloTo;
	private final Map<String, String> map;

	public HelloWorld() {
		this("World");
	}
	
	public HelloWorld(String helloTo) {
		if(helloTo != null) {
			this.helloTo = new String("to " + helloTo);
		}
		this.map = new HashMap<String, String>();
		map.put("Hello", helloTo);
	}
	
	public String sayHello() {
		String to = map.get("Hello");
		return "Hello, " + helloTo + ", and " + to +"!";
	}
	
	public static void main(String[] args) {
		HelloWorld hello;
		
		hello = new HelloWorld();
		System.out.println(hello.sayHello());
		
		hello = new HelloWorld("Barry");
		System.out.println(hello.sayHello());
	}
}
