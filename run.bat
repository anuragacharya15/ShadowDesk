@echo off
REM Shadowfax Internal Helpdesk & Support Ticket Management System
REM Compilation and Execution Script

echo [SHADOWFAX HELPDESK] Compiling Java source files...
if not exist "bin" mkdir bin

javac -encoding UTF-8 -d bin src\main\java\com\shadowfax\helpdesk\model\*.java src\main\java\com\shadowfax\helpdesk\exception\*.java src\main\java\com\shadowfax\helpdesk\util\*.java src\main\java\com\shadowfax\helpdesk\repository\*.java src\main\java\com\shadowfax\helpdesk\service\*.java src\main\java\com\shadowfax\helpdesk\web\*.java src\main\java\com\shadowfax\helpdesk\gui\*.java src\main\java\com\shadowfax\helpdesk\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please check your JDK setup.
    exit /b %ERRORLEVEL%
)

echo [SHADOWFAX HELPDESK] Launching application...
echo.
java -cp bin com.shadowfax.helpdesk.Main %*
