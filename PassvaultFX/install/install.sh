#!/bin/bash

echo "Configuring PassvaultFX Client"
CWD=`pwd`
cd ..
INSTALL_DIR=`pwd`
cd $CWD
echo "Installing into ${INSTALL_DIR}"


function mac_setup {
   echo "Setting up Mac PassvaultFX Client"
   mkdir -p ${INSTALL_DIR}/PassvaultFX.app/Contents/MacOS
   mkdir ${INSTALL_DIR}/PassvaultFX.app/Contents/Resources
   cp ./mac/vault.icns ${INSTALL_DIR}/PassvaultFX.app/Contents/Resources
   cp ./mac/info.plist ${INSTALL_DIR}/PassvaultFX.app/Contents
   cp ./mac/PassvaultFX ${INSTALL_DIR}/PassvaultFX.app/Contents/MacOS
   sed -i "" "s|<VAULT_DIR>|${INSTALL_DIR}|g" ${INSTALL_DIR}/PassvaultFX.app/Contents/MacOS/PassvaultFX
   echo "PassvaultFX.app setup.."
}

function linux_setup {
   # tested on Ubuntu
   echo "Setting up Linux PassvaultFX Client"
   cp ./linux/passvaultFX.desktop ${INSTALL_DIR}
   sed -e "s|<VAULT_DIR>|${INSTALL_DIR}|g" -i ${INSTALL_DIR}/passvaultFX.desktop
   chmod 755 ${INSTALL_DIR}/passvaultFX.desktop
   echo "Copying shortcut to Desktop"
   cp ${INSTALL_DIR}/passvaultFX.desktop ~/Desktop
   echo "PassvaultFX setup.."
}


# create shared directory structure
mkdir ${INSTALL_DIR}/bin
mkdir ${INSTALL_DIR}/config
mkdir ${INSTALL_DIR}/lib
mkdir ${INSTALL_DIR}/logs

cp logging.properties ${INSTALL_DIR}/config
cp passvaultFX.sh ${INSTALL_DIR}/bin
mv jars/*.jar ${INSTALL_DIR}/lib


OS="`uname`"
case $OS in
  'Linux')
    OS='Linux'
    sed -e "s|<VAULT_DIR>|${INSTALL_DIR}|g" -i ${INSTALL_DIR}/bin/passvaultFX.sh
    linux_setup
    ;;
  'Darwin') 
    OS='Mac'
    sed -i "" "s|<VAULT_DIR>|${INSTALL_DIR}|g" ${INSTALL_DIR}/bin/passvaultFX.sh
    mac_setup
    ;;
  *) 
    # for everything else for now just setup sh script
    	sed -e "s|<VAULT_DIR>|${INSTALL_DIR}|g" -i passvault.sh
    	echo "To start run ${INSTALL_DIR}/passvault.sh"
    ;;
esac


echo "done"

