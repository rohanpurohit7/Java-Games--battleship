@echo off
set "JAVA_HOME=C:\Users\rpuro\.jdks\temurin-24.0.2"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
call gradlew.bat run
pause
