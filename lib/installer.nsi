Name "DBEdit 2"

OutFile "..\dist\DBEdit_setup.exe"

InstallDir "$PROGRAMFILES\DBEdit 2"

InstallDirRegKey HKLM "Software\DBEdit 2" InstallLocation

Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

Section Install
  SetOutPath $INSTDIR
  Delete "$INSTDIR\*.*"
  File ..\src\license.txt
  File ..\lib\*.jar
  File ..\dist\DBEdit.exe
  WriteRegStr HKLM "Software\DBEdit 2" InstallLocation $INSTDIR
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DBEdit 2" "DisplayName" "DBEdit 2"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DBEdit 2" "UninstallString" '"$INSTDIR\uninstall.exe"'
  CreateDirectory "$SMPROGRAMS\DBEdit 2"
  CreateShortCut "$SMPROGRAMS\DBEdit 2\DBEdit 2.lnk" "$INSTDIR\DBEdit.exe"
  CreateShortCut "$SMPROGRAMS\DBEdit 2\Uninstall.lnk" "$INSTDIR\uninstall.exe"
  WriteUninstaller "uninstall.exe"
SectionEnd

Section Uninstall
  DeleteRegKey HKLM "Software\DBEdit 2"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DBEdit 2"
  RMDir /r "$SMPROGRAMS\DBEdit 2"
  RMDir /r "$INSTDIR"
SectionEnd
