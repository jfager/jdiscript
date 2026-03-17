package org.jdiscript.example;

/**
 * A test class that throws various exceptions, useful for debugging scripts
 * that track exception behavior.
 */
public class ExceptionExample {

    public static int safeDivide(int a, int b) {
        try {
            return a / b;
        } catch (ArithmeticException e) {
            System.out.println("Caught: " + e.getMessage());
            return 0;
        }
    }

    public static String safeCharAt(String s, int index) {
        try {
            return String.valueOf(s.charAt(index));
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("Caught: " + e.getMessage());
            return "";
        }
    }

    public static Object safeArrayAccess(Object[] arr, int index) {
        try {
            return arr[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Caught: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("Result: " + safeDivide(10, 2));
        System.out.println("Result: " + safeDivide(10, 0));
        System.out.println("Result: " + safeCharAt("hello", 1));
        System.out.println("Result: " + safeCharAt("hello", 99));
        System.out.println("Result: " + safeArrayAccess(new Object[]{"a", "b"}, 0));
        System.out.println("Result: " + safeArrayAccess(new Object[]{"a", "b"}, 5));
    }
}
