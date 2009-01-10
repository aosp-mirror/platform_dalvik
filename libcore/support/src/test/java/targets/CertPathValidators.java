package targets;

import dalvik.annotation.VirtualTestTarget;
import java.security.cert.CertPathValidator;

/**
 * @hide
 */
public interface CertPathValidators {
    /**
     * @hide
     */
    abstract class Internal extends CertPathValidator {
        protected Internal() {
            super(null, null, null);
        }
    }

    @VirtualTestTarget
    static abstract class PKIX extends Internal {
        protected abstract void method();
    }

}
