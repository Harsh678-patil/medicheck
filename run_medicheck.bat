@echo off
echo ==========================================
echo MediCheck Application Launcher
echo ==========================================
echo.

echo Checking for Maven...
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo Maven not found in PATH! 
    echo Please run this project directly from your IDE ^(VS Code / IntelliJ^)
    echo Or install Maven manually from https://maven.apache.org/
    pause
    exit /b
)

echo Compiling and running the application...
call mvn clean compile exec:java -Dexec.mainClass="com.medicheck.Main"

pause
