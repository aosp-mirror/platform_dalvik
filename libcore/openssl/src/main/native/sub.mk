# This file is included by the top-level libcore Makefile.
# It's not a normal makefile, so we don't include CLEAR_VARS
# or BUILD_*_LIBRARY.

LOCAL_SRC_FILES := \
	BNInterface.c

LOCAL_C_INCLUDES += \
	external/openssl/include

# Any shared/static libs that are listed here must also
# be listed in libs/nativehelper/Makefile.
# TODO: fix this requirement

LOCAL_SHARED_LIBRARIES += \
	libcrypto

LOCAL_STATIC_LIBRARIES +=
