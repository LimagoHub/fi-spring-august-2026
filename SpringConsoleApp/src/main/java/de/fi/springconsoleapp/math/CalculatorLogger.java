package de.fi.springconsoleapp.math;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("logger")
public class CalculatorLogger implements Calculator {

    private final Calculator calculator;

    public CalculatorLogger(@Qualifier("impl") Calculator calculator) {
        this.calculator = calculator;
    }

    @Override
    public double add(final double a, final double b) {
        System.out.println("Adding " + a + " " + b);
        return calculator.add(a, b);
    }

    @Override
    public double sub(final double a, final double b) {
        return calculator.sub(a, b);
    }
}
