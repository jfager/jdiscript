package cern.jarrace.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * Main entry point for the controller, instanciates an embedded Jetty container server, loads the
 * spring {@link org.springframework.web.servlet.DispatcherServlet} and configures all
 * the {@link org.springframework.web.bind.annotation.RestController}s
 * @author timartin
 */

@SpringBootApplication
public class Application {
    public static void main(String args[]) {
        ApplicationContext context = SpringApplication.run(Application.class, args);
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        System.out.println("******** Bean Count ********");
        System.out.println(context.getBeanDefinitionCount());
    }
}