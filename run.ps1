# Shadowfax Internal Helpdesk - PowerShell Run Script
Write-Host "[SHADOWFAX HELPDESK] Compiling Java source files..." -ForegroundColor Cyan
if (!(Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }

$javaFiles = Get-ChildItem -Path "src\main\java" -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -d bin $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SHADOWFAX HELPDESK] Launching application...`n" -ForegroundColor Green
    java -cp bin com.shadowfax.helpdesk.Main
} else {
    Write-Host "[ERROR] Compilation failed. Please verify your JDK installation." -ForegroundColor Red
}
