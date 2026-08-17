package de.fi.springconsoleapp.runner;

import de.fi.springconsoleapp.math.Calculator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
public class MyRunner implements CommandLineRunner {

    @Qualifier("secure")
    private final Calculator calculator;


    @Override
    public void run(String... args) throws Exception {
        System.out.println("Hello World");
        System.out.println(calculator.add(1, 2));
    }
}
