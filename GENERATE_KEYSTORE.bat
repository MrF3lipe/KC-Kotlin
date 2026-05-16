@echo off
chcp 65001 >nul 2>&1
title Generar Keystore - Kitchen Cabinet
echo ========================================================
echo   GENERAR KEYSTORE PARA FIRMAR APK RELEASE
echo ========================================================
echo.
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java no instalado. Instala JDK 17 primero.
    pause & exit /b 1
)
echo [OK] Java detectado
echo.
:inputs
set /p KEY_ALIAS=Alias (ej: kitchencabinet): 
set /p KEYSTORE_PASS=Contrasena del keystore: 
set /p KEY_PASS=Contrasena de la clave (ENTER=igual): 
if "%KEY_PASS%"=="" set KEY_PASS=%KEYSTORE_PASS%
echo.
echo [*] Generando keystore...
keytool -genkey -v -keystore keystore.jks -alias %KEY_ALIAS% -keyalg RSA -keysize 2048 -validity 10000 -storepass %KEYSTORE_PASS% -keypass %KEY_PASS%
if %ERRORLEVEL% equ 0 (
    echo.
    echo ========================================================
    echo   KEYSTORE GENERADO: keystore.jks
    echo ========================================================
    echo.
    echo   CONFIGURACION PARA GITHUB SECRETS:
    echo.
    certutil -encode keystore.jks keystore.b64
    echo.
    echo   KEYSTORE_PASSWORD: %KEYSTORE_PASS%
    echo   KEY_ALIAS: %KEY_ALIAS%
    echo   KEY_PASSWORD: %KEY_PASS%
    echo.
    echo   IMPORTANTE:
    echo   - Copia esto arriba en GitHub > Settings > Secrets > Actions
    echo   - NO subas keystore.jks a GitHub
    echo   - Guarda las contrasenas en un lugar seguro
) else (
    echo [ERROR] No se pudo generar el keystore
)
echo.
pause
