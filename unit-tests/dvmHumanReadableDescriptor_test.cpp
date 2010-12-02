#include <gtest/gtest.h>

// TODO: make dalvik's .h files C++-clean.
extern "C" char* dvmHumanReadableDescriptor(const char* descriptor);

TEST(dvmHumanReadableDescriptor, ArrayReferences) {
  ASSERT_STREQ("java.lang.Class[]", dvmHumanReadableDescriptor("[Ljava/lang/Class;"));
  ASSERT_STREQ("java.lang.Class[][]", dvmHumanReadableDescriptor("[[Ljava/lang/Class;"));
}

TEST(dvmHumanReadableDescriptor, ScalarReferences) {
  ASSERT_STREQ("java.lang.String", dvmHumanReadableDescriptor("Ljava.lang.String;"));
  ASSERT_STREQ("java.lang.String", dvmHumanReadableDescriptor("Ljava/lang/String;"));
}

TEST(dvmHumanReadableDescriptor, PrimitiveArrays) {
  ASSERT_STREQ("boolean[]", dvmHumanReadableDescriptor("[Z"));
  ASSERT_STREQ("boolean[][]", dvmHumanReadableDescriptor("[[Z"));
  ASSERT_STREQ("byte[]", dvmHumanReadableDescriptor("[B"));
  ASSERT_STREQ("byte[][]", dvmHumanReadableDescriptor("[[B"));
  ASSERT_STREQ("char[]", dvmHumanReadableDescriptor("[C"));
  ASSERT_STREQ("char[][]", dvmHumanReadableDescriptor("[[C"));
  ASSERT_STREQ("double[]", dvmHumanReadableDescriptor("[D"));
  ASSERT_STREQ("double[][]", dvmHumanReadableDescriptor("[[D"));
  ASSERT_STREQ("float[]", dvmHumanReadableDescriptor("[F"));
  ASSERT_STREQ("float[][]", dvmHumanReadableDescriptor("[[F"));
  ASSERT_STREQ("int[]", dvmHumanReadableDescriptor("[I"));
  ASSERT_STREQ("int[][]", dvmHumanReadableDescriptor("[[I"));
  ASSERT_STREQ("long[]", dvmHumanReadableDescriptor("[J"));
  ASSERT_STREQ("long[][]", dvmHumanReadableDescriptor("[[J"));
  ASSERT_STREQ("short[]", dvmHumanReadableDescriptor("[S"));
  ASSERT_STREQ("short[][]", dvmHumanReadableDescriptor("[[S"));
}

TEST(dvmHumanReadableDescriptor, PrimitiveScalars) {
  ASSERT_STREQ("boolean", dvmHumanReadableDescriptor("Z"));
  ASSERT_STREQ("byte", dvmHumanReadableDescriptor("B"));
  ASSERT_STREQ("char", dvmHumanReadableDescriptor("C"));
  ASSERT_STREQ("double", dvmHumanReadableDescriptor("D"));
  ASSERT_STREQ("float", dvmHumanReadableDescriptor("F"));
  ASSERT_STREQ("int", dvmHumanReadableDescriptor("I"));
  ASSERT_STREQ("long", dvmHumanReadableDescriptor("J"));
  ASSERT_STREQ("short", dvmHumanReadableDescriptor("S"));
}
