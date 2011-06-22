# Copyright (C) 2008 The Android Open Source Project
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


LOCAL_PATH:= $(call my-dir)

#
# Common definitions.
#

dalvikvm_src_files := \
    Main.cpp

dalvikvm_c_includes := \
    $(JNI_H_INCLUDE) \
    dalvik/include


#
# Build for the target (device).
#

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(dalvikvm_src_files)
LOCAL_C_INCLUDES := $(dalvikvm_c_includes)

LOCAL_SHARED_LIBRARIES := \
    libdvm \
    libssl \
    libz

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := dalvikvm

include $(BUILD_EXECUTABLE)


#
# Build for the host.
#

ifeq ($(WITH_HOST_DALVIK),true)

    include $(CLEAR_VARS)
    LOCAL_SRC_FILES := $(dalvikvm_src_files)
    LOCAL_C_INCLUDES := $(dalvikvm_c_includes)

    ifeq ($(HOST_OS)-$(HOST_ARCH),darwin-x86)
        # OS X comes with all these libraries, so there is no need
        # to build any of them. Note: OpenSSL consists of libssl
        # and libcrypto.
        LOCAL_LDLIBS := -lffi -lssl -lcrypto -lz
    else
        LOCAL_LDLIBS += -ldl -lpthread
        LOCAL_SHARED_LIBRARIES += libdvm libcrypto libicuuc libicui18n libssl
    endif

    LOCAL_MODULE_TAGS := optional
    LOCAL_MODULE := dalvikvm

    include $(BUILD_HOST_EXECUTABLE)

endif
