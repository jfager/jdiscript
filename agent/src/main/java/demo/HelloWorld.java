package demo;


class HelloWorld implements Inspectable {

    public static void main(String[] args) throws Exception {
        new HelloWorld().run();
    }

    @Override
    public void run() {
        System.out.println("I'm a Simple Program");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done");
    }
}
