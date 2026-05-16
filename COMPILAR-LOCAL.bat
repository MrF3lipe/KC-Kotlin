@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

echo ════════════════════════════════════════════════════════════════
echo   Kitchen Cabinet KOTLIN — Compilar APK localmente
echo ════════════════════════════════════════════════════════════════
echo.

:: ── Detectar local.properties ─────────────────────────────────────────────────
if not exist "local.properties" (
    echo [*] Creando local.properties...
    if defined ANDROID_HOME (
        echo sdk.dir=%ANDROID_HOME:\=\\%> local.properties
        echo [OK] sdk.dir=%ANDROID_HOME%
    ) else if exist "C:\android-sdk" (
        echo sdk.dir=C\:\\android-sdk> local.properties
        echo [OK] sdk.dir=C:\android-sdk (detectado automáticamente)
    ) else (
        echo [ERROR] No se encontró Android SDK.
        echo         Crea local.properties manualmente con:
        echo           sdk.dir=C\:\\ruta\\al\\android-sdk
        pause & exit /b 1
    )
    echo.
)

:: ── Configurar Gradle local (opcional) ────────────────────────────────────────
if exist "C:\android-sdk\gradle\gradle-8.9-bin.zip" (
    echo [*] Usando Gradle local: gradle-8.9-bin.zip
    powershell -NoProfile -Command ^
        "(Get-Content 'gradle\wrapper\gradle-wrapper.properties') -replace 'distributionUrl=.*', 'distributionUrl=file\:///C\:/android-sdk/gradle/gradle-8.9-bin.zip' | Set-Content 'gradle\wrapper\gradle-wrapper.properties'"
    echo [OK] Wrapper apuntado a Gradle local.
) else (
    echo [INFO] No se encontró Gradle local en C:\android-sdk\gradle\
    echo        Usará distribución online: https://services.gradle.org
)
echo.

:: ── Compilar ──────────────────────────────────────────────────────────────────
echo [*] Compilando Debug APK...
call gradlew.bat assembleDebug --stacktrace

if %ERRORLEVEL% equ 0 (
    echo.
    echo ════════════════════════════════════════════════════════════════
    echo   APK compilado correctamente:
    echo   app\build\outputs\apk\debug\app-debug.apk
    echo ════════════════════════════════════════════════════════════════
) else (
    echo.
    echo ════════════════════════════════════════════════════════════════
    echo   [ERROR] La compilación falló. Revisa los mensajes arriba.
    echo ════════════════════════════════════════════════════════════════
)
echo.
pause
