#!/bin/sh

rootDir=`pwd`
androidDir=/home/damien/ics

cd "${androidDir}"
source build/envsetup.sh
lunch 2
cd "${rootDir}"
