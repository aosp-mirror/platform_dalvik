import java.lang.reflect.Member;
import java.security.Permission;

public class Enforcer extends SecurityManager {
    public static final Enforcer THE_ONE = new Enforcer();

    /** whether to deny the next request */
    private boolean deny;

    /**
     * Not publicly constructable. Use {@link #THE_ONE}.
     */
    private Enforcer() {
        deny = false;
    }

    /**
     * Deny the next request.
     */
    public void denyNext() {
        deny = true;
    }

    /**
     * Throw an exception if the instance had been asked to deny a request.
     */
    private void denyIfAppropriate() {
        if (deny) {
            deny = false;
            throw new SecurityException("Denied!");
        }
    }

    public void checkPackageAccess(String pkg) {
        System.out.println("checkPackageAccess: " + pkg);
        denyIfAppropriate();
        super.checkPackageAccess(pkg);
    }

    public void checkMemberAccess(Class c, int which) {
        String member;

        switch (which) {
            case Member.DECLARED: member = "DECLARED"; break;
            case Member.PUBLIC:   member = "PUBLIC";   break;
            default:              member = "<" + which + ">?"; break;
        }

        System.out.println("checkMemberAccess: " + c.getName() + ", " +
                member);
        denyIfAppropriate();
        super.checkMemberAccess(c, which);
    }

    public void checkPermission(Permission perm) {
        System.out.println("checkPermission: " + perm);
        denyIfAppropriate();
        super.checkPermission(perm);
    }

    public void checkPermission(Permission perm, Object context) {
        System.out.println("checkPermission: " + perm + ", " + context);
        denyIfAppropriate();
        super.checkPermission(perm, context);
    }
}
