LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
        java/dalvik/jtreg/Adb.java \
        java/dalvik/jtreg/Command.java \
        java/dalvik/jtreg/CommandFailedException.java \
        java/dalvik/jtreg/Dx.java \
        java/dalvik/jtreg/Dalvikvm.java \
        java/dalvik/jtreg/ExpectedResult.java \
        java/dalvik/jtreg/Javac.java \
        java/dalvik/jtreg/JtregRunner.java \
        java/dalvik/jtreg/Run.java \
        java/dalvik/jtreg/Strings.java \
        java/dalvik/jtreg/TestDescriptions.java \
        java/dalvik/jtreg/TestRunner.java \
        java/dalvik/jtreg/TestToDex.java \

LOCAL_MODULE:= dalvik_jtreg
LOCAL_STATIC_JAVA_LIBRARIES := javatest jh jtreg

include $(BUILD_HOST_JAVA_LIBRARY)

include $(call all-subdir-makefiles)

# prebuilt javatest.jar
include $(CLEAR_VARS)
LOCAL_PREBUILT_JAVA_LIBRARIES := javatest:lib/javatest.jar
include $(BUILD_HOST_PREBUILT)

# prebuilt jh.jar
include $(CLEAR_VARS)
LOCAL_PREBUILT_JAVA_LIBRARIES := jh:lib/jh.jar
include $(BUILD_HOST_PREBUILT)

# prebuilt jtreg.jar
include $(CLEAR_VARS)
LOCAL_PREBUILT_JAVA_LIBRARIES := jtreg:lib/jtreg.jar
include $(BUILD_HOST_PREBUILT)
