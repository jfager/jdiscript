package org.jdiscript.util;


public class Utils {
	
    public static String repeat(String s, int times) {
        StringBuilder out = new StringBuilder();
        for(int i=0; i<times; i++) {
            out.append(s);
        }
        return out.toString();
    }
    
    public static void println(String s) {
        System.out.println(s);
    }
    
    public interface Block {
        void go() throws Exception;
    }
    
    public interface NoArgFn<T> {
        T go() throws Exception;
    }
    
    public static void unchecked(Block t) {
        try {
            t.go();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T> T unchecked(NoArgFn<T> f) {
        try {
            return f.go();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
