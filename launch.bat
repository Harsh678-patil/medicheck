@echo off
title MediCheck Frontend Launcher
echo ==========================================
echo MediCheck Application - Auto Launcher
echo ==========================================
echo.

if not exist ".maven\bin\mvn.cmd" (
    echo [INFO] Portable Maven not found. Downloading it automatically...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile 'maven.zip'"
    echo [INFO] Extracting Maven...
    powershell -Command "Expand-Archive -Path 'maven.zip' -DestinationPath '.maven' -Force"
    del maven.zip
    move .maven\apache-maven-3.9.6\* .maven\ >nul 2>nul
    rmdir /S /Q .maven\apache-maven-3.9.6 >nul 2>nul
)

echo [INFO] Compiling and starting the UI... Please wait...
call .maven\bin\mvn clean compile exec:java -Dexec.mainClass="com.medicheck.Main" -q

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Something went wrong!
    pause
)
