@echo off
title Push SpendWise to GitHub
echo ========================================================
echo Pushing SpendWise project to GitHub:
echo https://github.com/Anilsoni27/SpendWise-Tracker
echo ========================================================
echo.
cd /d "%~dp0"
set "PATH=C:\Users\anils\.tools\git\cmd;C:\Users\anils\.tools\git\mingw64\bin;%PATH%"

echo Checking Git status...
git status -s

echo.
echo Pushing main branch to origin...
git push -u origin main

echo.
if %ERRORLEVEL% EQU 0 (
    echo ========================================================
    echo [SUCCESS] SpendWise successfully pushed to GitHub!
    echo Check it out at: https://github.com/Anilsoni27/SpendWise-Tracker
    echo ========================================================
) else (
    echo ========================================================
    echo [ERROR] Push failed. If prompted for GitHub login, please sign in.
    echo ========================================================
)
echo.
pause
