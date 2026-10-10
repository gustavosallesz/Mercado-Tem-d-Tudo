@echo off
setlocal
cd /d "%~dp0"

call build.bat
if errorlevel 1 exit /b 1

if not exist build\package-input mkdir build\package-input
copy /y dist\MercadoTemdTudo.jar build\package-input\MercadoTemdTudo.jar >nul
if errorlevel 1 exit /b 1

if exist dist\windows rmdir /s /q dist\windows
if not exist dist\windows mkdir dist\windows
if exist dist\MercadoTemdTudo-Windows.zip del /q dist\MercadoTemdTudo-Windows.zip

jpackage --type app-image --name "Mercado Tem d'Tudo" --app-version 1.4.0 --vendor "Luiz Gustavo Salles" --input build\package-input --main-jar MercadoTemdTudo.jar --main-class MercadoApp --dest dist\windows
if errorlevel 1 exit /b 1

jar --create --file dist\MercadoTemdTudo-Windows.zip -C dist\windows "Mercado Tem d'Tudo"
if errorlevel 1 exit /b 1

echo Pacote Windows criado em dist\MercadoTemdTudo-Windows.zip
endlocal