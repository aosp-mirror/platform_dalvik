# This file is included by the top-level libcore Android.mk.
# It's not a normal makefile, so we don't include CLEAR_VARS
# or BUILD_*_LIBRARY.

LOCAL_SRC_FILES := \
	org_apache_harmony_xnet_provider_jsse_NativeCrypto.cpp \
	org_apache_harmony_xnet_provider_jsse_OpenSSLSessionImpl.cpp \
	org_apache_harmony_xnet_provider_jsse_OpenSSLServerSocketImpl.cpp \
	org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl.cpp

LOCAL_C_INCLUDES += \
	external/openssl/include


# Any shared/static libs that are listed here must also
# be listed in libs/nativehelper/Android.mk.
# TODO: fix this requirement

LOCAL_SHARED_LIBRARIES += \
	libcrypto \
	libssl \
    libutils
    
LOCAL_STATIC_LIBRARIES +=
