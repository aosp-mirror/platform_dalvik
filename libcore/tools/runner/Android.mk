LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

ext_dirs := \
        ../../../../external/jsr305/ri/src/main/java \
        ../../../../external/guava/src \
        ../../../../external/caliper/src

ext_src_files := $(call all-java-files-under,$(ext_dirs))

LOCAL_SRC_FILES := \
        $(ext_src_files) \
        java/dalvik/runner/Aapt.java \
        java/dalvik/runner/Adb.java \
        java/dalvik/runner/ActivityMode.java \
        java/dalvik/runner/CaliperFinder.java \
        java/dalvik/runner/CaliperRunner.java \
        java/dalvik/runner/Classpath.java \
        java/dalvik/runner/CodeFinder.java \
        java/dalvik/runner/Command.java \
        java/dalvik/runner/CommandFailedException.java \
        java/dalvik/runner/DalvikRunner.java \
        java/dalvik/runner/DeviceDalvikVm.java \
        java/dalvik/runner/Driver.java \
        java/dalvik/runner/Dx.java \
        java/dalvik/runner/Environment.java \
        java/dalvik/runner/EnvironmentDevice.java \
        java/dalvik/runner/EnvironmentHost.java \
        java/dalvik/runner/ExpectedResult.java \
        java/dalvik/runner/JUnitFinder.java \
        java/dalvik/runner/JUnitRunner.java \
        java/dalvik/runner/JavaVm.java \
        java/dalvik/runner/Javac.java \
        java/dalvik/runner/JtregFinder.java \
        java/dalvik/runner/JtregRunner.java \
        java/dalvik/runner/MainFinder.java \
        java/dalvik/runner/MainRunner.java \
        java/dalvik/runner/Mkdir.java \
        java/dalvik/runner/Mode.java \
        java/dalvik/runner/NamingPatternCodeFinder.java \
        java/dalvik/runner/Option.java \
        java/dalvik/runner/OptionParser.java \
        java/dalvik/runner/Result.java \
        java/dalvik/runner/Rm.java \
        java/dalvik/runner/Strings.java \
        java/dalvik/runner/TestProperties.java \
        java/dalvik/runner/TestRun.java \
        java/dalvik/runner/TestRunner.java \
        java/dalvik/runner/Threads.java \
        java/dalvik/runner/Vm.java \
        java/dalvik/runner/XmlReportPrinter.java \

LOCAL_MODULE:= dalvik_runner
LOCAL_STATIC_JAVA_LIBRARIES := javatest jh jtreg kxml2-2.3.0

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
