package targets;

import dalvik.annotation.VirtualTestTarget;

import java.security.SecureRandom;

/**
 * @hide
 */
public interface SecureRandoms {
    /**
     * @hide
     */
    abstract class Internal extends SecureRandom {
        protected Internal() {
            super();
        }
    }

    @VirtualTestTarget
    static abstract class SHAPRNG1 extends Internal {
        protected abstract void method();
    }
}
