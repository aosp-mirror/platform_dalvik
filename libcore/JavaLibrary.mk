# -*- mode: makefile -*-
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
# Definitions for building the Java library and associated tests.
#

#
# Common definitions for host and target.
#

# The core library is divided into modules. Each module has a separate
# Java source directory, and some (hopefully eventually all) also have
# a directory for tests.

define all-core-java-files
$(patsubst ./%,%,$(shell cd $(LOCAL_PATH) && find */src/$(1)/java -name "*.java"))
endef

define all-test-java-files-under
$(patsubst ./%,%,$(shell cd $(LOCAL_PATH) && find $(1)/src/test/java -name "*.java"))
endef

# Redirect ls stderr to /dev/null because the corresponding resources
# directories don't always exist.
define all-core-resource-dirs
$(shell cd $(LOCAL_PATH) && ls -d */src/test/{java,resources} 2> /dev/null)
endef

# The core Java files and associated resources.
core_src_files := $(call all-core-java-files,main)
core_resource_dirs := $(call all-core-resource-dirs,main)

# The tests' associated resources.
test_resource_dirs := $(call all-core-resource-dirs)


#
# Build for the target (device).
#

# Definitions to make the core library.

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(core_src_files)
LOCAL_JAVA_RESOURCE_DIRS := $(core_resource_dirs)

LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_DX_FLAGS := --core-library

LOCAL_NO_EMMA_INSTRUMENT := true
LOCAL_NO_EMMA_COMPILE := true

LOCAL_MODULE := core

include $(BUILD_JAVA_LIBRARY)

core-intermediates := ${intermediates}




# Definitions to make the core-tests libraries.
#
# We make a library per module, because otherwise the .jar files get too
# large, to the point that dx(1) can't cope (and the build is
# ridiculously slow).
#
# TODO: DalvikRunner will make this nonsense obsolete.

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,annotation)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-annotation
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,archive)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-archive
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,concurrent)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-concurrent
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,crypto)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-crypto
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,dom)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-dom
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,icu)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-icu
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,logging)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-logging
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,luni-kernel)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-luni-kernel
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,luni)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
# This module contains the top-level "tests.AllTests" that ties everything
# together, so it has compile-time dependencies on all the other test
# libraries.
# TODO: we should have a bogus module that just contains tests.AllTests for speed.
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_JAVA_LIBRARIES += core-tests-annotation core-tests-archive
LOCAL_JAVA_LIBRARIES += core-tests-concurrent core-tests-crypto
LOCAL_JAVA_LIBRARIES += core-tests-dom core-tests-icu core-tests-logging
LOCAL_JAVA_LIBRARIES += core-tests-luni-kernel core-tests-math core-tests-nio
LOCAL_JAVA_LIBRARIES += core-tests-nio_char core-tests-prefs core-tests-regex
LOCAL_JAVA_LIBRARIES += core-tests-security core-tests-sql core-tests-suncompat
LOCAL_JAVA_LIBRARIES += core-tests-text core-tests-x-net core-tests-xml
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-luni
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,math)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-math
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,nio)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-nio
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,nio_char)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-nio_char
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,prefs)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-prefs
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,regex)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-regex
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,security)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-security
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,sql)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-sql
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,suncompat)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-suncompat
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,support)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-support
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,text)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-text
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,x-net)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-x-net
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-test-java-files-under,xml)
LOCAL_JAVA_RESOURCE_DIRS := $(test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core caliper core-tests-support
LOCAL_DX_FLAGS := --core-library
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := core-tests-xml
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


#
# Build for the host.
#

ifeq ($(WITH_HOST_DALVIK),true)

    # Definitions to make the core library.

    include $(CLEAR_VARS)

    LOCAL_SRC_FILES := $(core_src_files)
    LOCAL_JAVA_RESOURCE_DIRS := $(core_resource_dirs)

    LOCAL_NO_STANDARD_LIBRARIES := true
    LOCAL_DX_FLAGS := --core-library

    LOCAL_NO_EMMA_INSTRUMENT := true
    LOCAL_NO_EMMA_COMPILE := true

    LOCAL_MODULE := core

    include $(BUILD_HOST_JAVA_LIBRARY)

endif
