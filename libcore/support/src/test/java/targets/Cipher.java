package targets;

import dalvik.annotation.VirtualTestTarget;

/**
 * @hide
 */
public interface Cipher {
    /**
     * @hide
     */
    abstract class Internal {
        protected Internal() {
        }
    }

    @VirtualTestTarget
    static abstract class RSA extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class AES extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class AESWrap extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class DES extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class DESede extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class DESedeWrap extends Internal {
        protected abstract void method();
    }

    @VirtualTestTarget
    static abstract class PBE extends Internal {
        protected abstract void method();
    }
}
