# Copyright (C) 2007 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# Definitions for building the native code needed for the core library.
#

#
# Common definitions for host and target.
#

# Get the list of all native directories that contain sub.mk files.
# We're using "sub.mk" to make it clear that these are not typical
# android makefiles.
define all-core-native-dirs
$(patsubst %/sub.mk,%,$(shell cd $(LOCAL_PATH) && ls -d */src/$(1)/native/sub.mk 2> /dev/null))
endef

# These two definitions are used to help sanity check what's put in
# sub.mk. See, the "error" directives immediately below.
core_magic_local_target := ...//::default:://...
core_local_path := $(LOCAL_PATH)

# Include a submakefile, resolve its source file locations,
# and stick them on core_src_files.  The submakefiles are
# free to append to LOCAL_SRC_FILES, LOCAL_C_INCLUDES,
# LOCAL_SHARED_LIBRARIES, or LOCAL_STATIC_LIBRARIES, but nothing
# else. All other LOCAL_* variables will be ignored.
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

# Set up the default state. Note: We use CLEAR_VARS here, even though
# we aren't quite defining a new rule yet, to make sure that the
# sub.mk files don't see anything stray from the last rule that was
# set up.
include $(CLEAR_VARS)
LOCAL_MODULE := $(core_magic_local_target)
core_src_files :=

# Include the sub.mk files.
$(foreach dir, \
    $(core_native_dirs), \
    $(eval $(call include-core-native-dir,$(dir))))

# Extract out the allowed LOCAL_* variables. Note: $(sort) also
# removes duplicates.
core_c_includes := $(sort dalvik/libcore/include $(LOCAL_C_INCLUDES) $(JNI_H_INCLUDE))
core_shared_libraries := $(sort $(LOCAL_SHARED_LIBRARIES))
core_static_libraries := $(sort $(LOCAL_STATIC_LIBRARIES))


#
# Build for the target (device).
#

include $(CLEAR_VARS)

ifeq ($(TARGET_ARCH),arm)
# Ignore "note: the mangling of 'va_list' has changed in GCC 4.4"
LOCAL_CFLAGS += -Wno-psabi
endif

# Define the rules.
LOCAL_SRC_FILES := $(core_src_files)
LOCAL_C_INCLUDES := $(core_c_includes)
LOCAL_SHARED_LIBRARIES := $(core_shared_libraries)
LOCAL_STATIC_LIBRARIES := $(core_static_libraries)
LOCAL_MODULE := libjavacore
include $(BUILD_STATIC_LIBRARY)

# Deal with keystores required for security. Note: The path to this file
# is hardcoded in TrustManagerFactoryImpl.java.
ALL_PREBUILT += $(TARGET_OUT)/etc/security/cacerts.bks
$(TARGET_OUT)/etc/security/cacerts.bks : $(LOCAL_PATH)/security/src/main/files/cacerts.bks | $(ACP)
	$(transform-prebuilt-to-target)


#
# Build for the host.
#

ifeq ($(WITH_HOST_DALVIK),true)

    include $(CLEAR_VARS)

    # Define the rules.
    LOCAL_SRC_FILES := $(core_src_files)
    LOCAL_C_INCLUDES := $(core_c_includes)
    LOCAL_SHARED_LIBRARIES := $(core_shared_libraries)
    LOCAL_STATIC_LIBRARIES := $(core_static_libraries)
    LOCAL_MODULE := libjavacore-host
    include $(BUILD_HOST_STATIC_LIBRARY)

    # TODO: Figure out cacerts.bks for the host.

endif
