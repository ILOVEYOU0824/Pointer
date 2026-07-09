@echo off
title SafeContract Launcher

echo ========================================
echo   SafeContract AI
echo ========================================
echo.
echo Starting backend and frontend...
echo.

start "SafeContract Backend" cmd /k "%~dp0start-backend.bat"

echo Waiting 8 seconds for backend...
timeout /t 8 /nobreak >nul

start "SafeContract Frontend" cmd /k "%~dp0start-frontend.bat"

echo.
echo Done! Open http://localhost:5173 in your browser.
echo.
pause
