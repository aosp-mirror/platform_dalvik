package targets;

import dalvik.annotation.VirtualTestTarget;

import java.security.MessageDigest;

/**
 * @hide
 */
public interface MessageDigests {
    /**
     * @hide
     */
    abstract class Internal extends MessageDigest {
        protected Internal() {
            super(null);
        }
    }

    @VirtualTestTarget
    static abstract class MD5 extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class SHA_1 extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class SHA_256 extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class SHA_384 extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class SHA_512 extends Internal {
        protected abstract void method();
    }
}
