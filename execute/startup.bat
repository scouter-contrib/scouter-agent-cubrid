@echo off

set SHELL_PATH=%~dp0
set ARGC=0
for %%i in (%*) do set /A ARGC+=1

if %ARGC% equ 4 (
set agent_cms_user=%1
set agent_cms_passwd=%2
set agent_dba_user=%3
set agent_dba_passwd=%4

java -Xmx128m -jar %SHELL_PATH%scouter-agent-cubrid.jar

) 

else GOTO :SHOW_USAGE


:SHOW_USAGE
@echo.Usage: %0 [CMS USER NAME] [CMS USER PASSWORD] [DBA USER NAME] [DBA USER PASSWORD]
@echo. Examples:
@echo. startup.bat admin admin dba "" (CUBRID Default)
GOTO :EOF
