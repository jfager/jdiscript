package org.jdiscript.example;

/**
 * A test class that spawns multiple threads, useful for debugging scripts
 * that track thread lifecycle and concurrency behavior.
 */
public class ThreadedExample {

    private final String name;

    public ThreadedExample(String name) {
        this.name = name;
    }

    public String doWork() {
        int sum = 0;
        for (int i = 0; i < 100; i++) {
            sum += i;
        }
        return name + ": " + sum;
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            ThreadedExample ex = new ThreadedExample("Worker-A");
            System.out.println(ex.doWork());
        }, "worker-a");

        Thread t2 = new Thread(() -> {
            ThreadedExample ex = new ThreadedExample("Worker-B");
            System.out.println(ex.doWork());
        }, "worker-b");

        Thread t3 = new Thread(() -> {
            ThreadedExample ex = new ThreadedExample("Worker-C");
            System.out.println(ex.doWork());
        }, "worker-c");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("All workers done.");
    }
}
