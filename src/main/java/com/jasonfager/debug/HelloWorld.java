package com.jasonfager.debug;

//A test class you can use for simple debugging scripts.
public class HelloWorld {
	public static final String HELLO_PROP_KEY
		= "com.jasonfager.jdiscript.helloTo";	

	private String helloTo = "World";
	
	public HelloWorld() {}
	public HelloWorld(String helloTo) {
		if(helloTo != null) {
			this.helloTo = helloTo;
		}
	}
	
	public String sayHello() {
		return "Hello, " + helloTo + "!";
	}
	
	public static void main(String[] args) {
		final HelloWorld hello;
		hello = new HelloWorld(System.getProperty(HELLO_PROP_KEY));
		System.out.println(hello.sayHello());
	}
}
