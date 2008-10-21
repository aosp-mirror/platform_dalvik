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
# Android.mk for Dalvik VM.  If you enable or disable optional features here,
# rebuild the VM with "make clean-libdvm && make libdvm".
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)


#
# Compiler defines.
#
LOCAL_CFLAGS += -fstrict-aliasing -Wstrict-aliasing=2 -fno-align-jumps

#
# Optional features.  These may impact the size or performance of the VM.
#
LOCAL_CFLAGS += -DWITH_PROFILER -DWITH_DEBUGGER

ifeq ($(WITH_DEADLOCK_PREDICTION),true)
  LOCAL_CFLAGS += -DWITH_DEADLOCK_PREDICTION
  WITH_MONITOR_TRACKING := true
endif
ifeq ($(WITH_MONITOR_TRACKING),true)
  LOCAL_CFLAGS += -DWITH_MONITOR_TRACKING
endif

#
# "Debug" profile:
# - debugger enabled
# - profiling enabled
# - tracked-reference verification enabled
# - allocation limits enabled
# - GDB helpers enabled
# - LOGV
# - assert()  (NDEBUG is handled in the build system)
#
ifeq ($(TARGET_BUILD_TYPE),debug)
LOCAL_CFLAGS += -DWITH_INSTR_CHECKS -DWITH_EXTRA_OBJECT_VALIDATION
LOCAL_CFLAGS += -DWITH_TRACKREF_CHECKS
LOCAL_CFLAGS += -DWITH_ALLOC_LIMITS
#LOCAL_CFLAGS += -DCHECK_MUTEX
#LOCAL_CFLAGS += -DPROFILE_FIELD_ACCESS
LOCAL_CFLAGS += -DDVM_SHOW_EXCEPTION=3
# add some extra stuff to make it easier to examine with GDB
LOCAL_CFLAGS += -DEASY_GDB
endif


#
# "Performance" profile:
# - all development features disabled
# - compiler optimizations enabled (redundant for "release" builds)
# - (debugging and profiling still enabled)
#
ifeq ($(TARGET_BUILD_TYPE),release)
#LOCAL_CFLAGS += -DNDEBUG -DLOG_NDEBUG=1
# "-O2" is redundant for device (release) but useful for sim (debug)
#LOCAL_CFLAGS += -O2 -Winline
LOCAL_CFLAGS += -DDVM_SHOW_EXCEPTION=1
# if you want to try with assertions on the device, add:
#LOCAL_CFLAGS += -UNDEBUG -DDEBUG=1 -DLOG_NDEBUG=1 -DWITH_DALVIK_ASSERT
endif

# bug hunting: checksum and verify interpreted stack when making JNI calls
#LOCAL_CFLAGS += -DWITH_JNI_STACK_CHECK

LOCAL_SRC_FILES := \
	AllocTracker.c \
	AtomicCache.c \
	CheckJni.c \
	Ddm.c \
	Debugger.c \
	DvmDex.c \
	Exception.c \
	Hash.c \
	Init.c \
	InlineNative.c.arm \
	Inlines.c \
	Intern.c \
	InternalNative.c \
	Jni.c \
	JarFile.c \
	LinearAlloc.c \
	Misc.c.arm \
	Native.c \
	PointerSet.c \
	Profile.c \
	Properties.c \
	RawDexFile.c \
	ReferenceTable.c \
	SignalCatcher.c \
	StdioConverter.c \
	Sync.c \
	Thread.c \
	UtfString.c \
	alloc/clz.c.arm \
	alloc/Alloc.c \
	alloc/HeapBitmap.c.arm \
	alloc/HeapDebug.c \
	alloc/HeapSource.c \
	alloc/HeapTable.c \
	alloc/HeapWorker.c \
	alloc/Heap.c.arm \
	alloc/MarkSweep.c.arm \
	alloc/DdmHeap.c \
	analysis/CodeVerify.c \
	analysis/DexOptimize.c \
	analysis/DexVerify.c \
	interp/Interp.c.arm \
	interp/InterpDbg.c.arm \
	interp/InterpStd.c.arm \
	interp/Stack.c \
	jdwp/ExpandBuf.c \
	jdwp/JdwpAdb.c \
	jdwp/JdwpConstants.c \
	jdwp/JdwpEvent.c \
	jdwp/JdwpHandler.c \
	jdwp/JdwpMain.c \
	jdwp/JdwpSocket.c \
	mterp/Mterp.c.arm \
	oo/AccessCheck.c \
	oo/Array.c \
	oo/Class.c \
	oo/Object.c \
	oo/Resolve.c \
	oo/TypeCheck.c \
	reflect/Annotation.c \
	reflect/Proxy.c \
	reflect/Reflect.c \
	test/TestHash.c

WITH_HPROF := $(strip $(WITH_HPROF))
ifeq ($(WITH_HPROF),)
  WITH_HPROF := true
endif
ifeq ($(WITH_HPROF),true)
  LOCAL_SRC_FILES += \
	hprof/Hprof.c \
	hprof/HprofClass.c \
	hprof/HprofHeap.c \
	hprof/HprofOutput.c \
	hprof/HprofString.c
  LOCAL_CFLAGS += -DWITH_HPROF=1

  ifeq ($(strip $(WITH_HPROF_UNREACHABLE)),true)
    LOCAL_CFLAGS += -DWITH_HPROF_UNREACHABLE=1
  endif

  ifeq ($(strip $(WITH_HPROF_STACK)),true)
    LOCAL_SRC_FILES += \
	hprof/HprofStack.c \
	hprof/HprofStackFrame.c
    LOCAL_CFLAGS += -DWITH_HPROF_STACK=1
  endif # WITH_HPROF_STACK
endif   # WITH_HPROF

ifeq ($(strip $(DVM_TRACK_HEAP_MARKING)),true)
  LOCAL_CFLAGS += -DDVM_TRACK_HEAP_MARKING=1
endif

LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE) \
	dalvik \
	dalvik/vm \
	external/zlib \
	$(KERNEL_HEADERS)

LOCAL_LDLIBS += -lpthread -ldl

ifeq ($(TARGET_SIMULATOR),true)
  ifeq ($(HOST_OS),linux)
    # need this for clock_gettime() in profiling
    LOCAL_LDLIBS += -lrt
  endif
endif

ifeq ($(TARGET_ARCH),arm)
	# use custom version rather than FFI
	#LOCAL_SRC_FILES += arch/arm/CallC.c
	LOCAL_SRC_FILES += arch/arm/CallOldABI.S arch/arm/CallEABI.S
	LOCAL_SRC_FILES += \
		mterp/out/InterpC-armv5.c.arm \
		mterp/out/InterpAsm-armv5.S
	LOCAL_SHARED_LIBRARIES += libdl
else
	# use FFI
	LOCAL_C_INCLUDES += external/libffi/$(TARGET_OS)-$(TARGET_ARCH)
	LOCAL_SRC_FILES += arch/generic/Call.c
	LOCAL_SRC_FILES += \
		mterp/out/InterpC-desktop.c \
		mterp/out/InterpAsm-desktop.S
	LOCAL_SHARED_LIBRARIES += libffi
endif

LOCAL_MODULE := libdvm

LOCAL_SHARED_LIBRARIES += \
	liblog \
	libcutils \
	libnativehelper \
	libz

LOCAL_STATIC_LIBRARIES += \
	libdex

include $(BUILD_SHARED_LIBRARY)

