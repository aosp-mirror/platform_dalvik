#!/bin/bash
#
# Copyright (C) 2010 The Android Open Source Project
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

# Test Dalvik Runner by exercising the various modes and various types of tests
#
# You can run this as follows

#   $ANDROID_BUILD_TOP/dalvik/libcore/tools/runner/test-dalvik-runner.sh

modes="host device activity"

# TODO: include dummy examples of each kind of 'runnable' we support,
# for test purposes instead of relying on external paths.
test_jtreg=/home/dalvik-prebuild/openjdk-6/jdk/test/java/util/HashMap/
test_junit=dalvik/libcore/logging/src/test/java/org/apache/harmony/logging/tests/java/util/logging/FilterTest.java
test_caliper=/home/bdc/benchmarks/caliper/caliper-read-only/src/examples/ArraySortBenchmark.java
test_main=external/junit/src/junit/textui/TestRunner.java
tests="$test_jtreg $test_junit $test_caliper $test_main"

cd $ANDROID_BUILD_TOP
. ./build/envsetup.sh
m core-tests junit caliper snod && adb reboot bootloader && fastboot flashall && adb wait-for-device
# when the device first comes up /sdcard is not mounted
while [ -z "`adb shell ls /sdcard | tr -d '\r\n'`" ] ; do sleep 1; done
mmm dalvik/libcore/tools/runner

#verbose=--verbose
#clean=--no-clean-after
extras="$verbose $clean"

dalvik_runner="java -cp out/host/linux-x86/framework/dalvik_runner.jar dalvik.runner.DalvikRunner"

for mode in $modes; do
    for test in $tests; do
        command="$dalvik_runner --mode $mode $extras $test"
        echo RUNNING $command
        $command
    done
done
