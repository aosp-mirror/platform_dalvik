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


#
# Android.mk for Dalvik VM.
#
# This makefile builds both for host and target, and so the very large
# swath of common definitions are factored out into a separate file to
# minimize duplication.
#
# Also, if you enable or disable optional features here (or Dvm.mk),
# rebuild the VM with "make clean-libdvm && make -j4 libdvm".
#

LOCAL_PATH:= $(call my-dir)

#
# Build for the target (device).
#

include $(CLEAR_VARS)

# Variables used in the included Dvm.mk.
dvm_os := $(TARGET_OS)
dvm_arch := $(TARGET_ARCH)
dvm_arch_variant := $(TARGET_ARCH_VARIANT)
dvm_simulator := $(TARGET_SIMULATOR)

include $(LOCAL_PATH)/Dvm.mk

# liblog and libcutils are shared for target.
LOCAL_SHARED_LIBRARIES += \
	liblog libcutils

LOCAL_MODULE := libdvm

include $(BUILD_SHARED_LIBRARY)


#
# Build for the host.
#

ifeq ($(WITH_HOST_DALVIK),true)

    include $(CLEAR_VARS)

    # Variables used in the included Dvm.mk.
    dvm_os := $(HOST_OS)
    dvm_arch := $(HOST_ARCH)
    dvm_arch_variant := $(HOST_ARCH_VARIANT)
    dvm_simulator := false

    include $(LOCAL_PATH)/Dvm.mk

    # liblog and libcutils are static for host.
    LOCAL_STATIC_LIBRARIES += \
        liblog libcutils

    # libffi is called libffi-host on the host. Similarly libnativehelper.
    LOCAL_SHARED_LIBRARIES := \
        $(patsubst libffi,libffi-host,$(LOCAL_SHARED_LIBRARIES))
    LOCAL_SHARED_LIBRARIES := \
        $(patsubst libnativehelper,libnativehelper-host,$(LOCAL_SHARED_LIBRARIES))

    LOCAL_MODULE := libdvm-host

    include $(BUILD_HOST_SHARED_LIBRARY)

endif
