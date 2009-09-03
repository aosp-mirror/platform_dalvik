#!/bin/bash
# java version >= 1.6 is required for this script.
# This script was tested to work with bouncycastle 1.32.

set -x
set -e

CERTSTORE=cacerts.bks

# Check java version.
JAVA_VERSION=`java -version 2>&1 | head -1`
JAVA_VERSION_MINOR=`expr match "$JAVA_VERSION" "java version \"[1-9]\.\([0-9]\).*\""`
if [ $JAVA_VERSION_MINOR -lt 6 ]; then
  echo "java version 1.6 or greater required for keytool usage"
  exit 255
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
      -provider org.bouncycastle.jce.provider.BouncyCastleProvider \
      -providerpath /usr/share/java/bcprov.jar \
      -storepass $STOREPASS
  let "COUNTER=$COUNTER + 1"
done
