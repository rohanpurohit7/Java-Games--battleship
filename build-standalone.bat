@echo off
setlocal

cd /d "%~dp0"

if not defined JAVA_HOME (
    echo JAVA_HOME must point to a JDK installation.
    exit /b 1
)

if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo JAVA_HOME does not contain javac.exe: "%JAVA_HOME%"
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

set "STANDALONE_CLASSES=out\standalone\classes"
set "STANDALONE_JAR=dist\battleship-command.jar"

if not exist "out\standalone" mkdir "out\standalone"
if not exist "%STANDALONE_CLASSES%" mkdir "%STANDALONE_CLASSES%"
if not exist "dist" mkdir "dist"

"%JAVA_HOME%\bin\javac.exe" ^
    --module-path "%JAVAFX_HOME%\lib" ^
    --add-modules javafx.controls,javafx.graphics ^
    -encoding UTF-8 ^
    -d "%STANDALONE_CLASSES%" ^
    src\main\java\navalbattle\*.java

if errorlevel 1 exit /b 1

"%JAVA_HOME%\bin\jar.exe" ^
    --create ^
    --file "%STANDALONE_JAR%" ^
    --main-class navalbattle.BattleshipFxApp ^
    -C "%STANDALONE_CLASSES%" .

if errorlevel 1 exit /b 1

echo Built %STANDALONE_JAR%
