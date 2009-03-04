package targets;

import dalvik.annotation.VirtualTestTarget;

import java.security.cert.CertificateFactory;

/**
 * @hide
 */
public interface CertificateFactories {
    /**
     * @hide
     */
    abstract class Internal extends CertificateFactory {
        protected Internal() {
            super(null, null, null);
        }
    }

    @VirtualTestTarget
    static abstract class X509 extends Internal {
        protected abstract void method();
    }

}
