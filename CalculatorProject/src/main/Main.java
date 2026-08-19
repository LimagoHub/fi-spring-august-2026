package main;

import client.CalcClient;
import common.LoggerProxy;
import math.Calculator;
import math.CalculatorImpl;
import math.CalculatorLogger;
import math.CalculatorSecure;

import java.time.Duration;
import java.time.Instant;

public class Main {

    public static void main(String[] args) {

        Instant start = Instant.now();
        //
        Instant end = Instant.now();
        var duration = Duration.between(start, end);
        System.out.println(duration.toMillis());


        Calculator calc = new CalculatorImpl();

        //calc = new CalculatorLogger(calc);
        calc = (Calculator) LoggerProxy.newInstance(calc);
        calc = new CalculatorSecure(calc);
        CalcClient calcClient = new CalcClient(calc);
        calcClient.go();
    }
}
