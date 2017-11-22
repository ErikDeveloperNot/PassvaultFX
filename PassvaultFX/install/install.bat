ECHO "Starting Installer"

FOR /f %%i in ('cd') do set CWD=%%i
CD ..
FOR /f %%i in ('cd') do set INSTALL_DIR=%%i
ECHO %INSTALL_DIR%
CD %CWD%

:: create shared directory structure
MKDIR %INSTALL_DIR%\bin
MKDIR %INSTALL_DIR%\config
MKDIR %INSTALL_DIR%\lib
MKDIR %INSTALL_DIR%\logs
MKDIR %INSTALL_DIR%\data

COPY logging.properties %INSTALL_DIR%\config
COPY passvaultFX.bat %INSTALL_DIR%\bin
MOVE jars\*.jar %INSTALL_DIR%\lib

ECHO "Running replace script"
cscript.exe .\windows\replace.vbs "%INSTALL_DIR%\bin\passvaultFX.bat" %INSTALL_DIR%

ECHO "Running create shortcut script"
cscript.exe .\windows\create_shortcut.vbs %INSTALL_DIR%

ECHO "Install done"