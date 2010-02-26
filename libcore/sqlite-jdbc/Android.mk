LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	src/main/native/sqlite_jni.c

LOCAL_C_INCLUDES += \
        $(JNI_H_INCLUDE) \
	external/sqlite/dist

LOCAL_SHARED_LIBRARIES += \
	libsqlite

LOCAL_STATIC_LIBRARIES +=

# This name is dictated by the fact that the SQLite code calls
# loadLibrary("sqlite_jni").
LOCAL_MODULE := libsqlite_jni

TARGET_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)
