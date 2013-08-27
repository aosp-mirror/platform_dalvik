# Copyright (C) 2012 The Pennsylvania State University
# Systems and Internet Infrastructure Security Laboratory
#
# Author: Damien Octeau <octeau@cse.psu.edu>
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
# USA.


#
# Dare, the Dalvik retargeting tool.
#
LOCAL_PATH:= $(call my-dir)

dare_src_files := \
		src/dare.cpp \
		src/main.cpp \
		src/class_file/annotation.cpp \
		src/class_file/utf8_info.cpp \
		src/class_file/flags.cpp \
		src/class_file/constant_pool.cpp \
		src/class_file/code_attribute.cpp \
		src/class_file/class_file.cpp \
		src/class_file/method_ref_info.cpp \
		src/class_file/stub.cpp \
		src/timer.cpp \
		src/tyde/cfg_builder.cpp \
		src/tyde/translator.cpp \
		src/tyde/tyde_instr_utils.cpp \
		src/tyde/tyde_instruction.cpp \
		src/typing/type.cpp \
		src/typing/type_element.cpp \
		src/typing/type_solver.cpp 
		

dare_c_includes := \
		$(LOCAL_PATH)/include \
		dalvik
		

dare_shared_libraries := 

dare_static_libraries := \
		libcutils \
		libdex 

##
##
## Build the host command line tool dare.
##
##
include $(CLEAR_VARS)
LOCAL_MODULE := dare-1.1.0
LOCAL_SRC_FILES := $(dare_src_files)
LOCAL_C_INCLUDES := $(dare_c_includes)
LOCAL_SHARED_LIBRARIES := $(dare_shared_libraries) 
LOCAL_STATIC_LIBRARIES := $(dare_static_libraries) 
LOCAL_LDLIBS += -lz
LOCAL_MODULE_TAGS := optional
include $(BUILD_HOST_EXECUTABLE)

