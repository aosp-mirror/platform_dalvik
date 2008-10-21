#!/bin/sh
#
# Run the core library tests.
#
# You can build and run the unit tests as follows (assuming sh/bash;
# csh users should modify to suit):
#
#   $ cd <client>/device
#   $ . envsetup.sh
#   $ lunch 2
#   $ make
#   $ make core-tests
#   $ ./dalvik/run-core-tests.sh
#
# Note: You may also specify a specific test as an argument.

datadir=/tmp/${USER}
base=$OUT
framework=$base/system/framework

export ANDROID_PRINTF_LOG=tag
export ANDROID_LOG_TAGS='*:w' # was: jdwp:i dalvikvm:i dalvikvmi:i'
export ANDROID_DATA=$datadir
export ANDROID_ROOT=$base/system

debug_opts=-Xcheck:jni
debug_opts="$debug_opts -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"

export LD_LIBRARY_PATH=$base/system/lib
export DYLD_LIBRARY_PATH=$base/system/lib

exe=$base/system/bin/dalvikvm
bpath=$framework/core.jar:$framework/ext.jar:$framework/framework.jar:$framework/core-tests.jar:$framework/http-tests.jar

# Notes:
# (1) The IO tests create lots of files in the current directory, so we change
#     to /tmp first.
# (2) Some of the core tests need a hell of a lot of memory, so we use a
#     large value for both heap and stack. 

rm -rf ${datadir}/xml_source
mkdir ${datadir}/xml_source
cp -R libcore/xml/src/test/resources/* ${datadir}/xml_source

cd $tmp
exec $valgrind $exe -Djava.io.tmpdir=$tmp -Xmx512M -Xss32K -Xbootclasspath:$bpath $debug_opts com.google.coretests.Main "$@"
