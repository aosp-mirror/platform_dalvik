LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	Command.java  \
	Filesystem.java \
	Git.java \
	Module.java \
	Modules.java \
	MappedDirectory.java \
	PullHarmonyCode.java \
	Svn.java

LOCAL_MODULE:= integrate

include $(BUILD_HOST_JAVA_LIBRARY)

include $(call all-subdir-makefiles)
