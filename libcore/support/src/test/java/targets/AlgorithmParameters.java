package targets;

import dalvik.annotation.VirtualTestTarget;

/**
 * @hide
 */
public interface AlgorithmParameters {
    /**
     * @hide
     */
    abstract class Internal extends java.security.AlgorithmParameters {
        protected Internal() {
            super(null, null, null);
        }
    }

    @VirtualTestTarget
    static abstract class DES extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class DSA extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class DESede extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class OAEP extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class AES extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class DH extends Internal {
        protected abstract void method();
    }
}
