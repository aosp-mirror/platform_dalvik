// Copyright 2008 Google Inc. All Rights Reserved.

/**
 * Class loader test.
 */
public class Main {
    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        FancyLoader loader;

        loader = new FancyLoader(ClassLoader.getSystemClassLoader());

        /*
         * This statement has no effect on this program, but it can
         * change the point where the LinkageException is thrown.  When
         * this is present the "reference implementation" throws an
         * exception from Class.newInstance(), when it's absent the
         * exception is deferred until the first time we call a method
         * that isn't actually implemented.
         *
         * This isn't the class that fails, but the VM thinks it has a
         * reference to one of these; presumably the difference is that
         * without this the VM finds itself holding a reference to an
         * instance of an uninitialized class.
         */
        System.out.println("base: " + DoubledImplement.class);

        /*
         * Run tests.
         */
        testAccess1(loader);
        testAccess2(loader);
        testAccess3(loader);

        testExtend(loader);
        testImplement(loader);
    }

    /**
     * See if we can load a class that isn't public to us.  We should be
     * able to load it but not instantiate it.
     */
    static void testAccess1(ClassLoader loader) {
        Class altClass;

        try {
            altClass = loader.loadClass("Inaccessible1");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("loadClass failed");
            cnfe.printStackTrace();
            return;
        }

        /* instantiate */
        Object obj;
        try {
            obj = altClass.newInstance();
            System.err.println("ERROR: Inaccessible1 was accessible");
        } catch (InstantiationException ie) {
            System.err.println("newInstance failed: " + ie);
            return;
        } catch (IllegalAccessException iae) {
            System.out.println("Got expected access exception #1");
            //System.out.println("+++ " + iae);
            return;
        }
    }

    /**
     * See if we can load a class whose base class is not accessible to it
     * (though the base *is* accessible to us).
     */
    static void testAccess2(ClassLoader loader) {
        Class altClass;

        try {
            altClass = loader.loadClass("Inaccessible2");
            System.err.println("ERROR: Inaccessible2 was accessible");
        } catch (ClassNotFoundException cnfe) {
            Throwable cause = cnfe.getCause();
            if (cause instanceof IllegalAccessError) {
                System.out.println("Got expected CNFE/IAE #2");
            } else {
                System.err.println("Got unexpected CNFE/IAE #2");
                cnfe.printStackTrace();
            }
        }
    }

    /**
     * See if we can load a class with an inaccessible interface.
     */
    static void testAccess3(ClassLoader loader) {
        Class altClass;

        try {
            altClass = loader.loadClass("Inaccessible3");
            System.err.println("ERROR: Inaccessible3 was accessible");
        } catch (ClassNotFoundException cnfe) {
            Throwable cause = cnfe.getCause();
            if (cause instanceof IllegalAccessError) {
                System.out.println("Got expected CNFE/IAE #3");
            } else {
                System.err.println("Got unexpected CNFE/IAE #3");
                cnfe.printStackTrace();
            }
        }
    }

    /**
     * Test a doubled class that extends the base class.
     */
    static void testExtend(ClassLoader loader) {
        Class doubledExtendClass;
        Object obj;

        /* get the "alternate" version of DoubledExtend */
        try {
            doubledExtendClass = loader.loadClass("DoubledExtend");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("loadClass failed: " + cnfe);
            return;
        }

        /* instantiate */
        try {
            obj = doubledExtendClass.newInstance();
        } catch (InstantiationException ie) {
            System.err.println("newInstance failed: " + ie);
            return;
        } catch (IllegalAccessException iae) {
            System.err.println("newInstance failed: " + iae);
            return;
        }

        /* use the base class reference to get a CL-specific instance */
        Base baseRef = (Base) obj;
        DoubledExtend de = baseRef.getExtended();

        /* try to call through it */
        try {
            String result;

            result = Base.doStuff(de);
            System.err.println("ERROR: did not get LinkageError on DE");
            System.err.println("(result=" + result + ")");
        } catch (LinkageError le) {
            System.out.println("Got expected LinkageError on DE");
            return;
        }
    }

    /**
     * Test a doubled class that implements a common interface.
     */
    static void testImplement(ClassLoader loader) {
        Class doubledImplementClass;
        Object obj;

        useImplement(new DoubledImplement(), true);

        /* get the "alternate" version of DoubledImplement */
        try {
            doubledImplementClass = loader.loadClass("DoubledImplement");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("loadClass failed: " + cnfe);
            return;
        }

        /* instantiate */
        try {
            obj = doubledImplementClass.newInstance();
        } catch (InstantiationException ie) {
            System.err.println("newInstance failed: " + ie);
            return;
        } catch (IllegalAccessException iae) {
            System.err.println("newInstance failed: " + iae);
            return;
        } catch (LinkageError le) {
            System.out.println("Got LinkageError on DI (early)");
            return;
        }

        /* if we lived this long, try to do something with it */
        ICommon icommon = (ICommon) obj;
        useImplement(icommon.getDoubledInstance(), false);
    }

    /**
     * Do something with a DoubledImplement instance.
     */
    static void useImplement(DoubledImplement di, boolean isOne) {
        //System.out.println("useObject: " + di.toString() + " -- "
        //    + di.getClass().getClassLoader());
        try {
            di.one();
            if (!isOne) {
                System.err.println("ERROR: did not get LinkageError on DI");
            }
        } catch (LinkageError le) {
            if (!isOne) {
                System.out.println("Got LinkageError on DI (late)");
            } else {
                throw le;
            }
        }
    }
}

