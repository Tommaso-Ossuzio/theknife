@echo off
setlocal enabledelayedexpansion
title TheKnife
cd /d "%~dp0"

set "SERVER=bin\serverTK-4.0.jar"
set "CLIENT=bin\clientTK-4.0.jar"
if not exist "%SERVER%" set "SERVER=serverTK\target\serverTK-4.0.jar"
if not exist "%CLIENT%" set "CLIENT=clientTK\target\clientTK-4.0.jar"
if not exist "%SERVER%" set "SERVER=serverTK-4.0.jar"
if not exist "%CLIENT%" set "CLIENT=clientTK-4.0.jar"

where javaw >nul 2>&1
if errorlevel 1 goto :senzaJava
if not exist "%SERVER%" goto :senzaArchivi
if not exist "%CLIENT%" goto :senzaArchivi

echo Avvio del server TheKnife...
start "TheKnife - server" javaw -jar "%SERVER%"

echo.
echo Indicare le credenziali di PostgreSQL nella finestra del server e premere Avvia server.
echo Al primo avvio l'importazione del catalogo puo' richiedere qualche minuto.
echo.
echo Attesa del server sulla porta 8999...

set /a tentativi=0
:attesa
netstat -an | findstr ":8999" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 goto :avviaClient
set /a tentativi+=1
if !tentativi! geq 300 goto :scaduto
timeout /t 2 /nobreak >nul
goto :attesa

:avviaClient
echo Server pronto: avvio dell'applicazione.
start "TheKnife" javaw -jar "%CLIENT%"
exit /b 0

:scaduto
echo.
echo Il server non risulta in ascolto sulla porta 8999.
echo Controllare i messaggi nella finestra del server e riprovare.
pause
exit /b 1

:senzaArchivi
echo.
echo Archivi dell'applicazione non trovati.
echo Sono attesi bin\serverTK-4.0.jar e bin\clientTK-4.0.jar,
echo oppure il progetto compilato con: mvnw.cmd package
pause
exit /b 1

:senzaJava
echo.
echo Java non risulta installato o non e' presente nel PATH.
echo E' richiesta una versione compresa fra la 21 e la 25.
pause
exit /b 1
