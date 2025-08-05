@echo off
echo ========================================
echo MMU Companion - Quick GitHub Auto-Sync
echo ========================================
echo.

cd /d "%~dp0"
powershell -ExecutionPolicy Bypass -File "auto-sync.ps1" %*

echo.
echo Press any key to continue...
pause >nul
