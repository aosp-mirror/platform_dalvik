#!/bin/bash
#
# Copyright (C) 2009 The Android Open Source Project
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
# certimport.sh recreates the cacerts.bks file from the x509 CA
# certificates in the cacerts directory.
# 
# By convention, the filenames in the cacerts directory are in the
# format of <hash>.<n> where "hash" is the subject hash produced by:
# 
#     openssl x509 -subject_hash -in filename
#
# and the "n" is the the depth of the certificate along a chain, i.e.
# .0 for roots, .1 for an intermediate one level deep, etc.
#
# The filename itself is not important, and is around just for convention sake.
#
# usage is simply running ./certimport.sh from the scripts directory
# 
# java version >= 1.6 is required for this script.
# 
# This script was tested to work with bouncycastle 1.32.
#

set -x
set -e

CERTSTORE=cacerts.bks

# put required 1.6 VM at head of PATH
JDK6PATH=/usr/lib/jvm/java-6-sun/bin
if [ ! -e $JDK6PATH/java ] ; then
  set +x
  echo
  echo "WARNING: could not find $JDK6PATH/java but continuing anyway."
  echo "    you might consider making sure the expected JDK is installed"
  echo "    or updating its location in this script."
  echo
  set -x
fi
export PATH=$JDK6PATH:$PATH

# Check java version.
JAVA_VERSION=`java -version 2>&1 | head -1`
JAVA_VERSION_MINOR=`expr match "$JAVA_VERSION" "java version \"[1-9]\.\([0-9]\).*\""`
if [ $JAVA_VERSION_MINOR -lt 6 ]; then
  set +x
  echo
  echo "ERROR: java version 1.6 or greater required for keytool usage"
  echo
  exit 1
fi

PROVIDER_CLASS=org.bouncycastle.jce.provider.BouncyCastleProvider
PROVIDER_PATH=/usr/share/java/bcprov.jar

if [ ! -e $PROVIDER_PATH ] ; then
  set +x
  echo
  echo "ERROR: could not find provider path $PROVIDER_PATH. Try installing with:"
  echo "    sudo apt-get install libbcprov-java"
  echo
  exit 1
fi

if [ -a $CERTSTORE ]; then
    rm $CERTSTORE || exit 1
fi

if [ -z "$STOREPASS" ]; then
    STOREPASS=changeit
fi

COUNTER=0
for cert in `ls -1 cacerts`
  do
  yes | keytool \
      -import \
      -v \
      -trustcacerts \
      -alias $COUNTER \
      -file <(openssl x509 -in cacerts/$cert) \
      -keystore $CERTSTORE \
      -storetype BKS \
      -provider $PROVIDER_CLASS \
      -providerpath $PROVIDER_PATH \
      -storepass $STOREPASS
  let "COUNTER=$COUNTER + 1"
done
