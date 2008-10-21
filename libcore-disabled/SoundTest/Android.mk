LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := framework core

LOCAL_MODULE_TAGS := tests

LOCAL_SRC_FILES := $(call all-subdir-java-files)

#define all-core-resource-dirs
#$(shell cd $(LOCAL_PATH) && find resources)
#endef

#LOCAL_JAVA_RESOURCE_DIRS := $(call all-core-resource-dirs)

LOCAL_PACKAGE_NAME := SoundTest

include $(BUILD_PACKAGE)
