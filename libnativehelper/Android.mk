# Copyright (C) 2009 The Android Open Source Project
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


LOCAL_PATH := $(call my-dir)

#
# Common definitions for host and device.
#

src_files := \
	JNIHelp.c \
	Register.c

c_includes := \
	$(JNI_H_INCLUDE)

# Any shared/static libs required by libjavacore
# need to be mentioned here as well.
# TODO: fix this requirement

shared_libraries := \
	libexpat \
	libssl \
	libutils \
	libz \
	libcrypto  \
	libicudata \
	libicuuc   \
	libicui18n \
	libsqlite

static_libraries := \
	libjavacore \
	libfdlibm



#
# Build for the target (device).
#

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(src_files)
LOCAL_C_INCLUDES := $(c_includes)
LOCAL_SHARED_LIBRARIES := $(shared_libraries)
LOCAL_STATIC_LIBRARIES := $(static_libraries)

# liblog and libcutils are shared for target.
LOCAL_SHARED_LIBRARIES += \
	liblog libcutils

LOCAL_MODULE := libnativehelper

include $(BUILD_SHARED_LIBRARY)


#
# Build for the host.
#

ifeq ($(WITH_HOST_DALVIK),true)

    include $(CLEAR_VARS)

    LOCAL_SRC_FILES := $(src_files)
    LOCAL_C_INCLUDES := $(c_includes)
    LOCAL_SHARED_LIBRARIES := $(shared_libraries)
    LOCAL_STATIC_LIBRARIES := $(static_libraries)

    # liblog and libcutils are static for host.
    LOCAL_STATIC_LIBRARIES += \
        liblog libcutils

    LOCAL_MODULE := libnativehelper-host

    include $(BUILD_HOST_SHARED_LIBRARY)

endif
