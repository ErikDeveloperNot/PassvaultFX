#!/bin/bash

## set Java executable
#JAVA=

## passvault directory
VAULT_DIR=<VAULT_DIR>


## SSL setting
#TRUST_STORE=/opt/ssl/keystores/passvault_store.jks
#TRUST_PASSWORD=passvault

## Sync Debug logging
#SYNC_DEBUG=debug

## registration server
#REG_SERVER=localhost:8443
REG_SERVER=ec2-13-56-39-109.us-west-1.compute.amazonaws.com:8443

## extra java options
#JAVA_OPTS="-Djavax.net.debug=all"

################
# end variables
################

# check if JAVA was set, if so use it; if not see if it is in path, if not report/exit
if [ "$JAVA" != "" ]
then
  JAVA_EX=$JAVA
else
  if hash java 2> /dev/null
  then
    JAVA_EX=`which java` 
  else
    echo "Java needs to be installed, set JAVA environment variable or intstall Java"
    exit 1
  fi
fi

# if Mac setup doc icon and name (note name does not seem to work on later release of MacOS and will show Java instead)
OS="`uname`"
case $OS in
  'Darwin') 
    JAVA_OPTS="-Xdock:icon=${VAULT_DIR}/install/mac/vault.icns -Xdock:name=PassvaultFX $JAVA_OPTS"
    ;;
esac


# check if TRUST_STORE is set, if so add it to the JAVA_OPTS
if [ "$TRUST_STORE" != "" ]
then
  JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=$TRUST_STORE -Djavax.net.ssl.trustStorePassword=$TRUST_PASSWORD"
fi

cd $VAULT_DIR


${JAVA_EX} $JAVA_OPTS -Djava.util.logging.config.file=${VAULT_DIR}/config/logging.properties -Dcom.passvault.sync.logging=$SYNC_DEBUG -Dcom.passvault.register.server=$REG_SERVER -jar ${VAULT_DIR}/lib/passvaultFX.jar > ${VAULT_DIR}/logs/console.out 2>&1

