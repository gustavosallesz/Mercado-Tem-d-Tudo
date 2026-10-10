@echo off
setlocal
cd /d "%~dp0"

if not exist build\test-classes mkdir build\test-classes
javac --release 8 -encoding UTF-8 -d build\test-classes src\Produto.java src\ProdutoRepository.java src\MercadoApp.java tests\ProdutoRepositoryTest.java
if errorlevel 1 exit /b 1

java -Djava.awt.headless=true -cp build\test-classes ProdutoRepositoryTest
endlocal