@echo off
setlocal
cd /d "%~dp0"

if exist build rmdir /s /q build
mkdir build
if not exist dist mkdir dist

javac --release 8 -encoding UTF-8 -d build src\Produto.java src\ProdutoRepository.java src\MercadoApp.java
if errorlevel 1 exit /b 1

jar --create --file dist\MercadoTemdTudo.jar --main-class MercadoApp -C build .
if errorlevel 1 exit /b 1

echo JAR criado em dist\MercadoTemdTudo.jar
endlocal