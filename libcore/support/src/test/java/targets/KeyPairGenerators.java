package targets;

import dalvik.annotation.VirtualTestTarget;

import java.security.KeyPairGenerator;

/**
 * @hide
 */
public interface KeyPairGenerators {
    /**
     * @hide
     */
    abstract class Internal extends KeyPairGenerator {
        protected Internal() {
            super(null);
        }
    }

    @VirtualTestTarget
    static abstract class RSA extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class DSA extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class DH extends Internal {
        protected abstract void method();
    }

}
