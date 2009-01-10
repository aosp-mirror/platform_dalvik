package targets;

import dalvik.annotation.VirtualTestTarget;

import java.security.cert.CertPathBuilder;

/**
 * @hide
 */
public interface CertPathBuilders {
    /**
     * @hide
     */
    abstract class Internal extends CertPathBuilder {
        protected Internal() {
            super(null, null, null);
        }
    }

    @VirtualTestTarget
    static abstract class PKIX extends Internal {
        protected abstract void method();
    }

}
