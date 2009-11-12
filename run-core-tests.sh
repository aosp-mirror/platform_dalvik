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
#   $ make CtsCoreTests
#   $ ./dalvik/run-core-tests.sh
#
# Note: You may also specify a specific test as an argument.

datadir=/tmp/${USER}
base=$OUT
framework=$base/system/framework
apps=$base/data/app

export ANDROID_PRINTF_LOG=tag
export ANDROID_LOG_TAGS='*:w' # was: jdwp:i dalvikvm:i dalvikvmi:i'
export ANDROID_DATA=$datadir
export ANDROID_ROOT=$base/system

debug_opts=-Xcheck:jni

OPTS=`getopt -o dl: --long debug,log:,help -n $0 -- "$@"`

if [ $? != 0 ]; then echo "Terminating..." >&2; exit 1; fi

eval set -- "$OPTS"

while true; do
    case "$1" in
        -d|--debug) debug_opts="$debug_opts -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"; shift ;;
        -l) export ANDROID_LOG_TAGS='*:'$2; shift 2 ;;
        --log) export ANDROID_LOG_TAGS="$2"; shift 2 ;;
        --help)
            echo usage: $0 [-d\|--debug] [-l\|--log] test.class.name;
            printf "\t%-15s%s\n" "-d|--debug" "wait for the debugger";
            printf "\t%-15s%s\n" "-l" "set the global logging level";
            printf "\t%-15s%s\n" "--log" "set the logging TAG";
            printf "\t%-15s%s\n" "--help" "this message";
            exit 1;
        ;;
        --) shift; break ;;
        *) echo "Internal Error!" >&2; exit 1 ;;
    esac
done

export LD_LIBRARY_PATH=$base/system/lib
export DYLD_LIBRARY_PATH=$base/system/lib

exe=$base/system/bin/dalvikvm
bpath=$framework/core.jar:$framework/ext.jar:$framework/framework.jar
cpath=$framework/core-tests.jar

# Notes:
# (1) The IO tests create lots of files in the current directory, so we change
#     to /tmp first.
# (2) Some of the core tests need a hell of a lot of memory, so we use a
#     large value for both heap and stack. 

rm -rf ${datadir}/xml_source
mkdir -p ${datadir}/xml_source
mkdir -p ${datadir}/dalvik-cache
cd $ANDROID_BUILD_TOP/dalvik
cp -R libcore/xml/src/test/resources/* ${datadir}/xml_source

cd $datadir
exec $valgrind $exe \
     -Duser.language=en -Duser.region=US -Djava.io.tmpdir=$datadir \
     -Djavax.net.ssl.trustStore=$base/system/etc/security/cacerts.bks \
     -Xmx512M -Xss32K \
     -Xbootclasspath:$bpath -classpath $cpath $debug_opts \
     com.google.coretests.Main "$@"
