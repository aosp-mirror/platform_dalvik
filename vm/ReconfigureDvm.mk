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

include $(CLEAR_VARS)

# Variables used in the included Dvm.mk.
dvm_os := $(TARGET_OS)
ifndef TARGET_2ND_ARCH
dvm_arch := $(TARGET_ARCH)
dvm_arch_variant := $(TARGET_ARCH_VARIANT)
else
# Dalvik doesn't support 64-bit architectures, fall back to the 32-bit 2nd arch
dvm_arch := $(TARGET_2ND_ARCH)
dvm_arch_variant := $(TARGET_2ND_ARCH_VARIANT)
endif

include $(LOCAL_PATH)/Dvm.mk

LOCAL_SHARED_LIBRARIES += \
	libbacktrace \
	libcutils \
	libdl \
	liblog \
	libnativehelper \
	libselinux \
	libutils \
	libz

LOCAL_STATIC_LIBRARIES += libdex

LOCAL_C_INCLUDES += external/stlport/stlport bionic/ bionic/libstdc++/include
LOCAL_SHARED_LIBRARIES += libstlport

# Don't install on any build by default
LOCAL_MODULE_TAGS := optional
