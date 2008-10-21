// Copyright 2008 The Android Open Source Project

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Test java.lang.reflect.Proxy;
 */
public class Main {
    static Object createProxy(Object proxyMe) {
        /* declare an object that will handle the method calls */
        InvocationHandler handler = new MyInvocationHandler(proxyMe);

        /* create the proxy class */
        Class proxyClass = Proxy.getProxyClass(Shapes.class.getClassLoader(),
                            new Class[] { Quads.class, Colors.class });

        /* create a proxy object, passing the handler object in */
        Object proxy = null;
        try {
            Constructor<Class> cons;
            cons = proxyClass.getConstructor(
                            new Class[] { InvocationHandler.class });
            //System.out.println("Constructor is " + cons);
            proxy = cons.newInstance(new Object[] { handler });
        } catch (NoSuchMethodException nsme) {
            System.err.println("failed: " + nsme);
        } catch (InstantiationException ie) {
            System.err.println("failed: " + ie);
        } catch (IllegalAccessException ie) {
            System.err.println("failed: " + ie);
        } catch (InvocationTargetException ite) {
            System.err.println("failed: " + ite);
        }

        return proxy;
    }

    public static void main(String[] args) {
        Mix proxyMe = new Mix();
        Object proxy = createProxy(proxyMe);

        /* take it for a spin; verifies instanceof constraint */
        Shapes shapes = (Shapes) proxy;
        shapes.circle(3);
        shapes.rectangle(10, 20);
        shapes.blob();
        Quads quads = (Quads) proxy;
        quads.rectangle(15, 25);
        quads.trapezoid(6, 81.18, 4);
        Colors colors = (Colors) proxy;
        colors.red(1.0f);
        colors.blue(777);
        colors.mauve("sorry");
        colors.blob();

        /*
         * Exercise annotations on Proxy classes.  This is mostly to ensure
         * that annotation calls work correctly on generated classes.
         */
        System.out.println("");
        Method[] methods = proxy.getClass().getDeclaredMethods();
        System.out.println("Proxy methods: " + Arrays.deepToString(methods));
        Method meth = methods[methods.length -1];
        System.out.println("Decl annos: " + Arrays.deepToString(meth.getDeclaredAnnotations()));
        System.out.println("Param annos: " + Arrays.deepToString(meth.getParameterAnnotations()));
        Field[] fields = proxy.getClass().getDeclaredFields();
        System.out.println("Proxy fields: " + Arrays.deepToString(fields));
    }
}

/*
 * Some interfaces.
 */
interface Shapes {
    public void circle(int r);
    public int rectangle(int x, int y);

    public String blob();
}

interface Quads extends Shapes {
    public int rectangle(int x, int y);
    public int square(int x, int y);
    public int trapezoid(int x, double off, int y);
}

/*
 * More interfaces.
 */
interface Colors {
    public int red(float howRed);
    public int green(double howGreen);
    public double blue(int howBlue);
    public int mauve(String apology);

    public String blob();
}

/*
 * A class that implements them all.
 */
class Mix implements Quads, Colors {
    public void circle(int r) {
        System.out.println("--- circle " + r);
    }
    public int rectangle(int x, int y) {
        System.out.println("--- rectangle " + x + "," + y);
        return 4;
    }
    public int square(int x, int y) {
        System.out.println("--- square " + x + "," + y);
        return 4;
    }
    public int trapezoid(int x, double off, int y) {
        System.out.println("--- trap " + x + "," + y + "," + off);
        return 8;
    }
    public String blob() {
        System.out.println("--- blob");
        return "mix";
    }

    public int red(float howRed) {
        System.out.println("--- red " + howRed);
        return 0;
    }
    public int green(double howGreen) {
        System.out.println("--- green " + howGreen);
        return 1;
    }
    public double blue(int howBlue) {
        System.out.println("--- blue " + howBlue);
        return 2.54;
    }
    public int mauve(String apology) {
        System.out.println("--- mauve " + apology);
        return 3;
    }
}

/*
 * Invocation handler, defining the implementation of the proxy functions.
 */
class MyInvocationHandler implements InvocationHandler {
    Object mObj;

    public MyInvocationHandler(Object obj) {
        mObj = obj;
    }

    /*
     * This is called when anything gets invoked in the proxy object.
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {

        Object result = null;

        // Trap Object calls.  This is important here to avoid a recursive
        // invocation of toString() in the print statements below.
        if (method.getDeclaringClass() == java.lang.Object.class) {
            //System.out.println("!!! object " + method.getName());
            if (method.getName().equals("toString"))
                return super.toString();
            else if (method.getName().equals("hashCode"))
                return Integer.valueOf(super.hashCode());
            else if (method.getName().equals("equals"))
                return Boolean.valueOf(super.equals(args[0]));
            else
                throw new RuntimeException("huh?");
        }

        System.out.println("Invoke " + method);
        if (args == null || args.length == 0) {
            System.out.println(" (no args)");
        } else {
            for (int i = 0; i < args.length; i++)
                System.out.println(" " + i + ": " + args[i]);
        }

        try {
            if (true)
                result = method.invoke(mObj, args);
            else
                result = -1;
            System.out.println("Success: method " + method.getName()
                + " res=" + result);
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        }
        return result;
    }
}

