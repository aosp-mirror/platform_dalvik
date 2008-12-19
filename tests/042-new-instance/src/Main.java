// Copyright 2007 The Android Open Source Project

/**
 * Test instance creation.
 */
public class Main {
    public static void main(String[] args) {
        // should succeed
        try {
            Class c = Class.forName("LocalClass");
            Object obj = c.newInstance();
            System.out.println("LocalClass succeeded");
        } catch (Exception ex) {
            System.err.println("LocalClass failed");
            ex.printStackTrace();
        }

        // should fail
        try {
            Class c = Class.forName("otherpackage.PackageAccess");
            Object obj = c.newInstance();
            System.out.println("ERROR: PackageAccess succeeded unexpectedly");
        } catch (IllegalAccessException iae) {
            System.out.println("Got expected PackageAccess complaint");
        } catch (Exception ex) {
            System.err.println("Got unexpected PackageAccess failure");
            ex.printStackTrace();
        }

        LocalClass2.main();
    }
}

class LocalClass {
  // this class has a default constructor with package visibility
}


class LocalClass2 {
    public static void main() {
        try {
            CC.newInstance();
            System.out.println("LocalClass2 succeeded");
        } catch (Exception ex) {
            System.err.println("Got unexpected LocalClass2 failure");
            ex.printStackTrace();
        }
    }

    static class CC {
        private CC() {}

        static Object newInstance() {
            try {
                Class c = CC.class;
                return c.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
}

