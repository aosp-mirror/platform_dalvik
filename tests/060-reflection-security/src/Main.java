import other.Blort;

public class Main {
    static public boolean VERBOSE = false;

    static public void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("--verbose")) {
                VERBOSE = true;
            }
        }

        System.out.println("Setting SecurityManager.");
        System.setSecurityManager(Enforcer.THE_ONE);
        System.out.println("Running tests.");
        accessStuff();
        System.out.println("\nDone!");
    }

    static public void report(Throwable t) {
        if (VERBOSE) {
            t.printStackTrace(System.out);
        } else {
            System.out.println(t);
        }
    }

    static public void accessStuff() {
        Class c = other.Blort.class;

        /*
         * Note: We don't use reflection to factor out these tests,
         * becuase reflection also calls into the SecurityManager, and
         * we don't want to conflate the calls nor assume too much
         * in general about what calls reflection will cause.
         */

        System.out.println("\ngetFields A");
        try {
            c.getFields();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("getFields B");
        Enforcer.THE_ONE.denyNext();
        try {
            c.getFields();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("\ngetDeclaredFields A");
        try {
            c.getDeclaredFields();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("getDeclaredFields B");
        Enforcer.THE_ONE.denyNext();
        try {
            c.getDeclaredFields();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("\ngetMethods A");
        try {
            c.getMethods();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("getMethods B");
        Enforcer.THE_ONE.denyNext();
        try {
            c.getMethods();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("\ngetDeclaredMethods A");
        try {
            c.getDeclaredMethods();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("getDeclaredMethods B");
        Enforcer.THE_ONE.denyNext();
        try {
            c.getDeclaredMethods();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("\ngetConstructors A");
        try {
            c.getConstructors();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("getConstructors B");
        Enforcer.THE_ONE.denyNext();
        try {
            c.getConstructors();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("\ngetDeclaredConstructors A");
        try {
            c.getDeclaredConstructors();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("getDeclaredConstructors B");
        Enforcer.THE_ONE.denyNext();
        try {
            c.getDeclaredConstructors();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("\ngetClasses A");
        try {
            c.getClasses();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("getClasses B");
        Enforcer.THE_ONE.denyNext();
        try {
            c.getClasses();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("\ngetDeclaredClasses A");
        try {
            c.getDeclaredClasses();
        } catch (Exception ex) {
            report(ex);
        }

        System.out.println("getDeclaredClasses B");
        Enforcer.THE_ONE.denyNext();
        try {
            c.getDeclaredClasses();
        } catch (Exception ex) {
            report(ex);
        }
    }
}
