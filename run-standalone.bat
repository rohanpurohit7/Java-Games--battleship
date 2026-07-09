@echo off
setlocal

cd /d "%~dp0"

if not defined JAVA_HOME (
    echo JAVA_HOME must point to a JDK installation.
    exit /b 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo JAVA_HOME does not contain java.exe: "%JAVA_HOME%"
    exit /b 1
)

if not defined JAVAFX_HOME (
    echo JAVAFX_HOME must point to a JavaFX SDK directory, for example C:\javafx-sdk-21.0.5.
    exit /b 1
)

if not exist "%JAVAFX_HOME%\lib\javafx.controls.jar" (
    echo JAVAFX_HOME does not appear to contain a JavaFX SDK lib directory: "%JAVAFX_HOME%"
    exit /b 1
)

if not exist "dist\battleship-command.jar" (
    call "%~dp0build-standalone.bat"
    if errorlevel 1 exit /b 1
)

"%JAVA_HOME%\bin\java.exe" ^
    --module-path "%JAVAFX_HOME%\lib" ^
    --add-modules javafx.controls,javafx.graphics ^
    -jar "dist\battleship-command.jar"
