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
# Dare launcher.
#
LOCAL_PATH:= $(call my-dir)

launcher_src_files := \
		dare_launcher.cpp \
		main.cpp \

launcher_c_includes := 

launcher_shared_libraries := 

launcher_static_libraries := 

##
##
## Build the host command line tool dare-launcher.
##
##
include $(CLEAR_VARS)
LOCAL_MODULE := dare-launcher-1.1.0
LOCAL_SRC_FILES := $(launcher_src_files)
LOCAL_C_INCLUDES := $(launcher_c_includes)
LOCAL_SHARED_LIBRARIES := $(launcher_shared_libraries) 
LOCAL_STATIC_LIBRARIES := $(launcher_static_libraries)
LOCAL_MODULE_TAGS := optional
include $(BUILD_HOST_EXECUTABLE)

# The execution script.
# ============================================================
include $(CLEAR_VARS)
LOCAL_IS_HOST_MODULE := true
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE := dare
LOCAL_MODULE_TAGS := optional

include $(BUILD_SYSTEM)/base_rules.mk

#$(LOCAL_BUILT_MODULE): $(HOST_OUT_JAVA_LIBRARIES)/dare
$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/dare | $(ACP)
	@echo "Copy: $(PRIVATE_MODULE) ($@)"
	$(copy-file-to-new-target)
	$(hide) chmod 755 $@
