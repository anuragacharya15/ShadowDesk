@echo off
REM Shadowfax Internal Helpdesk - Automated Test Suite Runner

echo [SHADOWFAX HELPDESK] Compiling main sources and test suite...
if not exist "bin" mkdir bin

javac -encoding UTF-8 -d bin src\main\java\com\shadowfax\helpdesk\model\*.java src\main\java\com\shadowfax\helpdesk\exception\*.java src\main\java\com\shadowfax\helpdesk\util\*.java src\main\java\com\shadowfax\helpdesk\repository\*.java src\main\java\com\shadowfax\helpdesk\service\*.java src\main\java\com\shadowfax\helpdesk\web\*.java src\main\java\com\shadowfax\helpdesk\gui\*.java src\main\java\com\shadowfax\helpdesk\Main.java src\test\java\com\shadowfax\helpdesk\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Test compilation failed.
    exit /b %ERRORLEVEL%
)

echo [SHADOWFAX HELPDESK] Running 17 Verification Tests...
echo.
java -cp bin com.shadowfax.helpdesk.TicketSystemTestSuite
