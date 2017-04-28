# Bazel (https://bazel.io/) BUILD file for dex.
licenses(["notice"])  # Apache License 2.0

load("//tools/base/bazel:utils.bzl", "srcjar")

srcjar(
    name = "srcjar",
    java_library = ":dx",
    visibility = ["//tools/base/build-system/builder:__pkg__"],
)

java_library(
    name = "dx",
    srcs = glob([
        "src/**/*.java",
    ]),
    javacopts = ["-Xep:MissingCasesInEnumSwitch:WARN"],
)

java_test(
    name = "dx_tests",
    srcs = glob([
        "junit-tests/**/*.java",
    ]),
    jvm_flags = ["-Dtest.suite.jar=dx_tests.jar"],
    test_class = "com.android.testutils.JarTestSuite",
    deps = [
        ":dx",
        "//tools/base/testutils:tools.testutils",
        "//tools/base/third_party:junit_junit",
    ],
)
