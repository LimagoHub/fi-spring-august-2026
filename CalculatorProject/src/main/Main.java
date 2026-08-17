package main;

import client.CalcClient;
import common.LoggerProxy;
import math.Calculator;
import math.CalculatorImpl;
import math.CalculatorLogger;
import math.CalculatorSecure;

public class Main {

    public static void main(String[] args) {
        Calculator calc = new CalculatorImpl();

        //calc = new CalculatorLogger(calc);
        calc = (Calculator) LoggerProxy.newInstance(calc);
        calc = new CalculatorSecure(calc);
        CalcClient calcClient = new CalcClient(calc);
        calcClient.go();
    }
}
