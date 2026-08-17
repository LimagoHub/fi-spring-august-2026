package de.fi.springconsoleapp.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class MyRunner implements CommandLineRunner {

    // Calculator verwenden um zwei zahlen zu addieren und das Ergebnis in die Console schreiben

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Hello World");
    }
}
