package org.jdiscript.util;


public class Utils {

    private Utils() {}

    public static void println(String s) {
        System.out.println(s);
    }

    public interface Block {
        void go() throws Exception;
    }

    public static void unchecked(Block t) {
        try {
            t.go();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
