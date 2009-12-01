LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

# The core library is divided into modules. Each module has a separate Java
# source directory, and some (hopefully eventually all) also have a directory
# for tests. The two sections below define separate targets to build the
# core and the associated tests.

define all-core-java-files
$(patsubst ./%,%,$(shell cd $(LOCAL_PATH) && find */src/$(1)/java -name "*.java"))
endef

# Redirect ls stderr to /dev/null because the corresponding resources
# directory doesn't always exist.
define all-core-resource-dirs
$(shell cd $(LOCAL_PATH) && ls -d */src/$(1)/{java,resources} 2> /dev/null)
endef

LOCAL_SRC_FILES := $(call all-core-java-files,main)
LOCAL_JAVA_RESOURCE_DIRS := $(call all-core-resource-dirs,main)

LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_DX_FLAGS := --core-library

LOCAL_NO_EMMA_INSTRUMENT := true
LOCAL_NO_EMMA_COMPILE := true

LOCAL_MODULE := core

include $(BUILD_JAVA_LIBRARY)

core-intermediates := ${intermediates}

# Definitions to make the core-tests library.

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-core-java-files,test)
LOCAL_JAVA_RESOURCE_DIRS := $(call all-core-resource-dirs,test)

LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core
LOCAL_DX_FLAGS := --core-library

LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests

include $(BUILD_JAVA_LIBRARY)

# This one's tricky. One of our tests needs to have a
# resource with a "#" in its name, but Perforce doesn't
# allow us to submit such a file. So we create it here
# on-the-fly.
TMP_RESOURCE_DIR := $(OUT_DIR)/tmp/
TMP_RESOURCE_FILE := org/apache/harmony/luni/tests/java/lang/test\#.properties

$(TMP_RESOURCE_DIR)$(TMP_RESOURCE_FILE):
	@mkdir -p $(dir $@)
	@echo "Hello, world!" > $@

$(LOCAL_INTERMEDIATE_TARGETS): PRIVATE_EXTRA_JAR_ARGS := $(extra_jar_args) -C $(TMP_RESOURCE_DIR) $(TMP_RESOURCE_FILE)
$(LOCAL_INTERMEDIATE_TARGETS): $(TMP_RESOURCE_DIR)$(TMP_RESOURCE_FILE)

# Definitions for building a version of the core-tests.jar
# that is suitable for execution on the RI. This JAR would
# be better located in $HOST_OUT_JAVA_LIBRARIES, but it is
# not possible to refer to that from a shell script (the
# variable is not exported from envsetup.sh). There is also
# some trickery involved: we need to include some classes
# that reside in core.jar, but since we cannot incldue the
# whole core.jar in the RI classpath, we copy those classses
# over to our new file.
HOST_CORE_JAR := $(HOST_COMMON_OUT_ROOT)/core-tests.jar

$(HOST_CORE_JAR): PRIVATE_LOCAL_BUILT_MODULE := $(LOCAL_BUILT_MODULE)
$(HOST_CORE_JAR): PRIVATE_CORE_INTERMEDIATES := $(core-intermediates)
$(HOST_CORE_JAR): $(LOCAL_BUILT_MODULE)
	@rm -rf $(dir $<)/hostctsclasses
	$(call unzip-jar-files,$(dir $<)classes.jar,$(dir $<)hostctsclasses)
	@unzip -qx -o $(PRIVATE_CORE_INTERMEDIATES)/classes.jar dalvik/annotation/* -d $(dir $<)hostctsclasses
	@cp $< $@
	@jar uf $@ -C $(dir $<)hostctsclasses .

$(LOCAL_INSTALLED_MODULE): $(HOST_CORE_JAR)

$(LOCAL_INSTALLED_MODULE): run-core-tests

# Definitions to copy the core-tests runner script.

include $(CLEAR_VARS)
LOCAL_SRC_FILES := run-core-tests
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := run-core-tests
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := run-core-tests-on-ri
LOCAL_IS_HOST_MODULE := true
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := run-core-tests-on-ri
include $(BUILD_PREBUILT)

# Build all of the native code, if any is present.

include $(CLEAR_VARS)

# Get the list of all native directories that contain sub.mk files.
# We're using "sub.mk" to make it clear that these are not typical
# android makefiles.
define all-core-native-dirs
$(patsubst %/sub.mk,%,$(shell cd $(LOCAL_PATH) && ls -d */src/$(1)/native/sub.mk 2> /dev/null))
endef

core_magic_local_target := ...//::default:://...
core_local_path := $(LOCAL_PATH)

# Include a submakefile, resolve its source file locations,
# and stick them on core_src_files.  The submakefiles are
# free to append to LOCAL_C_INCLUDES, LOCAL_SHARED_LIBRARIES, etc.
#
# $(1): directory containing the makefile to include
define include-core-native-dir
    LOCAL_SRC_FILES :=
    include $(LOCAL_PATH)/$(1)/sub.mk
    ifneq ($$(LOCAL_MODULE),$(core_magic_local_target))
        $$(error $(LOCAL_PATH)/$(1)/sub.mk should not include CLEAR_VARS \
            or define LOCAL_MODULE)
    endif
    ifneq ($$(LOCAL_PATH),$(core_local_path))
        $$(error $(LOCAL_PATH)/$(1)/sub.mk should not define LOCAL_PATH)
    endif
    core_src_files += $$(addprefix $(1)/,$$(LOCAL_SRC_FILES))
    LOCAL_SRC_FILES :=
endef

# Find any native directories containing sub.mk files.
core_native_dirs := $(strip $(call all-core-native-dirs,main))
ifeq ($(core_native_dirs),)
    $(error No native code defined for libcore)
endif

# Set up the default state.
LOCAL_C_INCLUDES += dalvik/libcore/include $(JNI_H_INCLUDE)
LOCAL_MODULE := $(core_magic_local_target)
core_src_files :=

# Include the sub.mk files.
$(foreach dir, \
    $(core_native_dirs), \
    $(eval $(call include-core-native-dir,$(dir))))

# Define the rules.
LOCAL_SRC_FILES := $(core_src_files)
LOCAL_MODULE := libjavacore
include $(BUILD_STATIC_LIBRARY)

# Deal with keystores required for security. Note: The path to this file
# is hardcoded in TrustManagerFactoryImpl.java.
ALL_PREBUILT += $(TARGET_OUT)/etc/security/cacerts.bks
$(TARGET_OUT)/etc/security/cacerts.bks : $(LOCAL_PATH)/security/src/main/files/cacerts.bks | $(ACP)
	$(transform-prebuilt-to-target)
