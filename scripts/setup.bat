@echo off
REM KDS development environment setup
REM Just calls the PowerShell script with permission to run

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\setup-db.ps1"

REM Pause so the user can read output if they double-clicked
echo.
pause