package common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LoggerProxy implements java.lang.reflect.InvocationHandler {

    private Object obj;

    public static Object newInstance(Object obj) {
        return java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new LoggerProxy(obj));
    }

    private LoggerProxy(Object obj) {
        this.obj = obj;
    }

    public Object invoke(Object proxy, Method m, Object[] args)
            throws Throwable
    {
        Object result;
        try {

            // Before Advice
            System.out.println("before method " + m.getName());
            result = m.invoke(obj, args);
            // After returning (result ist verfuegbar)

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            // After Throwing Advice (Exception ist verfuegbar)
            throw new RuntimeException("unexpected invocation exception: " +
                    e.getMessage());
        } finally {
            // After Advice
            System.out.println("after method " + m.getName());
        }
        return result;
    }
}