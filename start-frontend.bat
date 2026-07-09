@echo off
title SafeContract Frontend

cd /d "%~dp0frontend"

echo ========================================
echo   SafeContract Frontend
echo ========================================
echo.
echo Browser: http://localhost:5173
echo.
echo Press Ctrl+C to stop.
echo.

call npm run dev

pause
