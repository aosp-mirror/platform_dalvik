// Copyright 2007 The Android Open Source Project

/*
 * This test requires compiling files twice and mixing the .class files.
 * Compile once and save AbstractBase.class.  Comment out the necessary
 * sections in both files, recompile, and save ConcreteSub.class.
 */

import java.lang.reflect.Method;

/**
 * Test insertion of an abstract method in a superclass.
 */
public class ConcreteSub extends AbstractBase {
    private static void callBase(AbstractBase abs) {
        System.out.println("calling abs.doStuff()");
        abs.doStuff();
    }
    
    public static void main() {
        ConcreteSub sub = new ConcreteSub();

        try {
            callBase(sub);
        } catch (AbstractMethodError ame) {
            System.out.println("Got expected exception from abs.doStuff().");
        }

        /*
         * Check reflection stuff.
         */
        Class absClass = AbstractBase.class;
        Method meth;

        System.out.println("class modifiers=" + absClass.getModifiers());

        try {
            meth = absClass.getMethod("redefineMe", (Class[]) null);
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
            return;
        }
        System.out.println("meth modifiers=" + meth.getModifiers());
    }
}

