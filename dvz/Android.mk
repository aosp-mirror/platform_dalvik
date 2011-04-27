# Copyright 2006 The Android Open Source Project

LOCAL_PATH := $(my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	dvz.cpp

LOCAL_SHARED_LIBRARIES := \
	libcutils

LOCAL_C_INCLUDES :=

LOCAL_CFLAGS :=

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := dvz

include $(BUILD_EXECUTABLE)
