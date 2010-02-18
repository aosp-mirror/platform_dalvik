LOCAL_PATH:= $(call my-dir)

# build DalvikRunner from the source under java/.
include $(CLEAR_VARS)
LOCAL_SRC_FILES :=  $(call all-java-files-under,java)
LOCAL_MODULE:= dalvik_runner
LOCAL_STATIC_JAVA_LIBRARIES := caliper javatest jh jtreg kxml2-2.3.0
# TODO this only works when junit is already built...
LOCAL_JAVA_LIBRARIES := junit
LOCAL_JAVACFLAGS := -Werror -Xlint:unchecked
include $(BUILD_HOST_JAVA_LIBRARY)

include $(call all-subdir-makefiles)

# prebuilt caliper.jar
include $(CLEAR_VARS)
LOCAL_PREBUILT_JAVA_LIBRARIES := caliper:lib/caliper.jar
include $(BUILD_HOST_PREBUILT)

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
