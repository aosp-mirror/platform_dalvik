#!/bin/bash
# java version >= 1.6 is required for this script.
# This script was tested to work with bouncycastle 1.32.

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
