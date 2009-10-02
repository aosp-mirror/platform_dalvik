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
# dexopt, the DEX file optimizer.  This is fully integrated with the VM,
# so it must be linked against the full VM shared library.
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
		OptMain.c

LOCAL_C_INCLUDES := \
		dalvik \
		dalvik/libdex \
		dalvik/vm \
		$(JNI_H_INCLUDE)

LOCAL_SHARED_LIBRARIES := \
		libcutils \
		liblog \
		libz \
		libssl \
		libdvm

LOCAL_MODULE := dexopt

include $(BUILD_EXECUTABLE)
