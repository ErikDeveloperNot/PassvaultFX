Set WshShell = CreateObject("WScript.Shell")

installDir= Wscript.Arguments(0)

strDesktop = WshShell.SpecialFolders("Desktop")
Set oMyShortCut= WshShell.CreateShortcut(strDesktop+"\PassvaultFX.lnk")

oMyShortCut.IconLocation = installDir+"\install\windows\vault.ico"
oMyShortCut.TargetPath = installDir+"\bin\passvaultFX.bat" 
oMyShortCut.WorkingDirectory = installDir
oMyShortCut.Description = "PassvaultFX"
oMyShortCut.Save
