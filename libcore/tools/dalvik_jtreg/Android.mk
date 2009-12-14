LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
        java/dalvik/jtreg/Adb.java \
        java/dalvik/jtreg/CaliperFinder.java \
        java/dalvik/jtreg/CaliperRunner.java \
        java/dalvik/jtreg/Classpath.java \
        java/dalvik/jtreg/Command.java \
        java/dalvik/jtreg/CommandFailedException.java \
        java/dalvik/jtreg/DeviceDalvikVm.java \
        java/dalvik/jtreg/Driver.java \
        java/dalvik/jtreg/Dx.java \
        java/dalvik/jtreg/ExpectedResult.java \
        java/dalvik/jtreg/Harness.java \
        java/dalvik/jtreg/JUnitFinder.java \
        java/dalvik/jtreg/JUnitRunner.java \
        java/dalvik/jtreg/JavaVm.java \
        java/dalvik/jtreg/Javac.java \
        java/dalvik/jtreg/JtregFinder.java \
        java/dalvik/jtreg/JtregRunner.java \
        java/dalvik/jtreg/Result.java \
        java/dalvik/jtreg/Strings.java \
        java/dalvik/jtreg/TestRun.java \
        java/dalvik/jtreg/TestFinder.java \
        java/dalvik/jtreg/TestRunner.java \
        java/dalvik/jtreg/Vm.java \
        java/dalvik/jtreg/XmlReportPrinter.java \

LOCAL_MODULE:= dalvik_jtreg
LOCAL_STATIC_JAVA_LIBRARIES := javatest jh jtreg kxml2-2.3.0 caliper

# TODO this only works when junit is already built...
LOCAL_JAVA_LIBRARIES := junit

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
