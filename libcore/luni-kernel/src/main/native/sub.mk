# This file is included by the top-level libcore Android.mk.
# It's not a normal makefile, so we don't include CLEAR_VARS
# or BUILD_*_LIBRARY.

LOCAL_SRC_FILES := \
	java_lang_ProcessManager.c \
	java_lang_System.c

LOCAL_C_INCLUDES +=

# Any shared/static libs that are listed here must also
# be listed in libs/nativehelper/Android.mk.
# TODO: fix this requirement

LOCAL_SHARED_LIBRARIES +=

LOCAL_STATIC_LIBRARIES +=
