@echo off
title SafeContract Backend

cd /d "%~dp0backend"

set GEMINI_MODEL=gemini-2.5-flash-lite
set GEMINI_VISION_MODEL=gemini-2.5-flash-lite
set CORS_ALLOWED_ORIGINS=http://localhost:5173

echo ========================================
echo   SafeContract Backend
echo ========================================
echo.
echo GEMINI_API_KEY is loaded from Windows env vars.
echo Server: http://localhost:8080
echo.
echo Press Ctrl+C to stop.
echo.

call mvnw.cmd spring-boot:run

pause
