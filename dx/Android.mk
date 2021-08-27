# Copyright 2006 The Android Open Source Project
#
LOCAL_PATH := $(call my-dir)

# We use copy-file-to-new-target so that the installed
# script files' timestamps are at least as new as the
# .jar files they wrap.

# This tool is prebuilt if we're doing an app-only build.
ifeq ($(TARGET_BUILD_APPS)$(filter true,$(TARGET_BUILD_PDK)),)

# the mainDexClasses rules
# ============================================================
include $(CLEAR_VARS)
LOCAL_IS_HOST_MODULE := true
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := mainDexClasses.rules
LOCAL_LICENSE_KINDS := SPDX-license-identifier-Apache-2.0
LOCAL_LICENSE_CONDITIONS := notice
LOCAL_NOTICE_FILE := $(LOCAL_PATH)/NOTICE

include $(BUILD_SYSTEM)/base_rules.mk

$(LOCAL_BUILT_MODULE): $(HOST_OUT_JAVA_LIBRARIES)/dx$(COMMON_JAVA_PACKAGE_SUFFIX)
$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/etc/mainDexClasses.rules | $(ACP)
	@echo "Copy: $(PRIVATE_MODULE) ($@)"
	$(copy-file-to-new-target)

INTERNAL_DALVIK_MODULES += $(LOCAL_INSTALLED_MODULE)

installed_mainDexClasses.rules := $(LOCAL_INSTALLED_MODULE)

# the mainDexClassesNoAapt rules
# ============================================================
include $(CLEAR_VARS)
LOCAL_IS_HOST_MODULE := true
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := mainDexClassesNoAapt.rules
LOCAL_LICENSE_KINDS := SPDX-license-identifier-Apache-2.0
LOCAL_LICENSE_CONDITIONS := notice
LOCAL_NOTICE_FILE := $(LOCAL_PATH)/NOTICE

include $(BUILD_SYSTEM)/base_rules.mk

$(LOCAL_BUILT_MODULE): $(HOST_OUT_JAVA_LIBRARIES)/dx$(COMMON_JAVA_PACKAGE_SUFFIX)
$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/etc/mainDexClassesNoAapt.rules | $(ACP)
	@echo "Copy: $(PRIVATE_MODULE) ($@)"
	$(copy-file-to-new-target)

INTERNAL_DALVIK_MODULES += $(LOCAL_INSTALLED_MODULE)

installed_mainDexClassesNoAapt.rules := $(LOCAL_INSTALLED_MODULE)

# the shrinkedAndroid jar is a library used by the mainDexClasses script
# ============================================================
include $(CLEAR_VARS)
LOCAL_IS_HOST_MODULE := true
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := shrinkedAndroid
LOCAL_LICENSE_KINDS := SPDX-license-identifier-Apache-2.0
LOCAL_LICENSE_CONDITIONS := notice
LOCAL_NOTICE_FILE := $(LOCAL_PATH)/NOTICE
LOCAL_BUILT_MODULE_STEM := shrinkedAndroid.jar
LOCAL_MODULE_SUFFIX := $(COMMON_JAVA_PACKAGE_SUFFIX)

include $(BUILD_SYSTEM)/base_rules.mk

$(LOCAL_BUILT_MODULE): PRIVATE_PROGUARD_FLAGS:= \
  -include $(addprefix $(LOCAL_PATH)/, shrinkedAndroid.proguard.flags)
$(LOCAL_BUILT_MODULE): $(call java-lib-files,$(call resolve-prebuilt-sdk-module,20)) \
                       $(addprefix $(LOCAL_PATH)/, shrinkedAndroid.proguard.flags)| $(PROGUARD)
	@echo Proguard: $@
	$(hide) $(PROGUARD) -injars "$<(**/*.class)" -outjars $@ $(PRIVATE_PROGUARD_FLAGS)

INTERNAL_DALVIK_MODULES += $(LOCAL_INSTALLED_MODULE)

installed_shrinkedAndroid := $(LOCAL_INSTALLED_MODULE)

# the mainDexClasses script
# ============================================================
include $(CLEAR_VARS)
LOCAL_IS_HOST_MODULE := true
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := mainDexClasses
LOCAL_LICENSE_KINDS := SPDX-license-identifier-Apache-2.0
LOCAL_LICENSE_CONDITIONS := notice
LOCAL_NOTICE_FILE := $(LOCAL_PATH)/NOTICE

include $(BUILD_SYSTEM)/base_rules.mk

$(LOCAL_BUILT_MODULE): $(HOST_OUT_JAVA_LIBRARIES)/dx$(COMMON_JAVA_PACKAGE_SUFFIX)
$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/etc/mainDexClasses | $(ACP)
	@echo "Copy: $(PRIVATE_MODULE) ($@)"
	$(copy-file-to-new-target)
	$(hide) chmod 755 $@

$(LOCAL_INSTALLED_MODULE): | $(installed_shrinkedAndroid) $(installed_mainDexClasses.rules) \
                             $(installed_mainDexClassesNoAapt.rules)
INTERNAL_DALVIK_MODULES += $(LOCAL_INSTALLED_MODULE)

endif # No TARGET_BUILD_APPS or TARGET_BUILD_PDK

# the jasmin script
# ============================================================
include $(CLEAR_VARS)
LOCAL_IS_HOST_MODULE := true
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := jasmin
LOCAL_LICENSE_KINDS := SPDX-license-identifier-Apache-2.0
LOCAL_LICENSE_CONDITIONS := notice
LOCAL_NOTICE_FILE := $(LOCAL_PATH)/NOTICE

include $(BUILD_SYSTEM)/base_rules.mk

$(LOCAL_BUILT_MODULE): $(HOST_OUT_JAVA_LIBRARIES)/jasmin.jar
$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/etc/jasmin | $(ACP)
	@echo "Copy: $(PRIVATE_MODULE) ($@)"
	$(copy-file-to-new-target)
	$(hide) chmod 755 $@

INTERNAL_DALVIK_MODULES += $(LOCAL_INSTALLED_MODULE)

# the jasmin lib
# ============================================================
include $(CLEAR_VARS)
LOCAL_IS_HOST_MODULE := true
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := jasmin.jar
LOCAL_LICENSE_KINDS := SPDX-license-identifier-Apache-2.0
LOCAL_LICENSE_CONDITIONS := notice
LOCAL_NOTICE_FILE := $(LOCAL_PATH)/NOTICE

include $(BUILD_SYSTEM)/base_rules.mk

$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/etc/jasmin.jar | $(ACP)
	@echo "Copy: $(PRIVATE_MODULE) ($@)"
	$(copy-file-to-target)
	$(hide) chmod 644 $@

INTERNAL_DALVIK_MODULES += $(LOCAL_INSTALLED_MODULE)
