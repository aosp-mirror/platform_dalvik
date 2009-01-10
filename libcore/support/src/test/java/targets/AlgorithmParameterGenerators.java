package targets;

import dalvik.annotation.VirtualTestTarget;

import java.security.AlgorithmParameterGenerator;

/**
 * @hide
 */
public interface AlgorithmParameterGenerators {
    /**
     * @hide
     */
    abstract class Internal extends AlgorithmParameterGenerator {
        protected Internal() {
            super(null, null, null);
        }
    }

    @VirtualTestTarget
    static abstract class AES extends Internal {
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
