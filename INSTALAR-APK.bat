@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

echo ════════════════════════════════════════════════════════════════
echo   Kitchen Cabinet KOTLIN — Instalar APK en dispositivo/emulador
echo ════════════════════════════════════════════════════════════════
echo.

set APK=app\build\outputs\apk\debug\app-debug.apk

if not exist "%APK%" (
    echo [ERROR] APK no encontrado: %APK%
    echo         Ejecuta primero COMPILAR-LOCAL.bat
    pause & exit /b 1
)

:: Buscar ADB
set ADB=
if defined ANDROID_HOME (
    if exist "%ANDROID_HOME%\platform-tools\adb.exe" set ADB=%ANDROID_HOME%\platform-tools\adb.exe
)
if not defined ADB (
    if exist "C:\android-sdk\platform-tools\adb.exe" set ADB=C:\android-sdk\platform-tools\adb.exe
)
if not defined ADB (
    where adb >nul 2>&1 && set ADB=adb
)
if not defined ADB (
    echo [ERROR] No se encontró adb.exe
    echo         Asegúrate de tener Android SDK Platform-Tools instalado.
    pause & exit /b 1
)

echo [*] Dispositivos conectados:
"%ADB%" devices
echo.

echo [*] Instalando APK...
"%ADB%" install -r "%APK%"

if %ERRORLEVEL% equ 0 (
    echo [OK] Instalado correctamente.
    echo [*] Lanzando app...
    "%ADB%" shell am start -n com.kitchencabinet/.MainActivity
) else (
    echo [ERROR] No se pudo instalar.
)
echo.
pause
