# This file is included by the top-level libcore Android.mk.
# It's not a normal makefile, so we don't include CLEAR_VARS
# or BUILD_*_LIBRARY.

LOCAL_SRC_FILES := \
	java_util_zip_Adler32.c \
	java_util_zip_CRC32.c \
	java_util_zip_Deflater.c \
	java_util_zip_Inflater.c \
  zipalloc.c \
	sieb.c

LOCAL_C_INCLUDES += \
	external/zlib

# Any shared/static libs that are listed here must also
# be listed in libs/nativehelper/Android.mk.
# TODO: fix this requirement

LOCAL_SHARED_LIBRARIES += \
	libz

LOCAL_STATIC_LIBRARIES +=
