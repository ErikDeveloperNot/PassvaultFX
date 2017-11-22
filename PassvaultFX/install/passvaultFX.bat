ECHO off
TITLE PassvaultFX

CD <VAULT_DIR>

SET LIB=<VAULT_DIR>\lib\*

:: registration server url
SET REG_SERVER=ec2-13-56-39-109.us-west-1.compute.amazonaws.com:8443

:: verbose logging for syncing
::SET SYNC_DEBUG=debug

java -cp %LIB% -D"java.util.logging.config.file=<VAULT_DIR>\config\logging.properties" -D"com.passvault.data.file=.\data\data.json" -D"com.passvault.register.server=%REG_SERVER%" -D"com.passvault.sync.logging=%SYNC_DEBUG%" -jar <VAULT_DIR>\lib\passvaultFX.jar > <VAULT_DIR>\logs\console.out 2> <VAULT_DIR>\logs\error.log
