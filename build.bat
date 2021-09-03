@echo off

set SHELL_PATH=%~dp0
set MVN_FILE=%MAVEN_HOME%\bin\mvn
call :FINDEXEC mvn MVN_FILE "%MVN_FILE%"

if NOT EXIST %MVN_FILE% echo "Please Install MAVEN". & GOTO :EOF

%MVN_FILE% -Dmaven.test.skip=true -f %SHELL_PATH%pom.xml clean package

:FINDEXEC
if EXIST %3 set %2="%~3"
if NOT EXIST %3 for %%X in (%1) do set FOUNDINPATH=%%~$PATH:X
if defined FOUNDINPATH set %2=%FOUNDINPATH:"=%
if NOT defined FOUNDINPATH if NOT EXIST %3 echo Executable [%1] is not found & GOTO :EOF
call echo Executable [%1] is found at [%%%2%%]
set FOUNDINPATH=
GOTO :EOF
