LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	JNIHelp.c \
	Register.c

LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE)

# Any shared/static libs required by libjavacore
# need to be mentioned here as well.
# TODO: fix this requirement

LOCAL_SHARED_LIBRARIES := \
	liblog \
	libcutils \
	libexpat \
	libssl \
	libutils \
	libz \
	libcrypto  \
	libicudata \
	libicuuc   \
	libicui18n \
	libsqlite

LOCAL_STATIC_LIBRARIES := \
	libjavacore \
	libfdlibm

LOCAL_MODULE := libnativehelper

include $(BUILD_SHARED_LIBRARY)
