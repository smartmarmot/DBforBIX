@echo off

SET mypath=%~dp0

if "%PROCESSOR_ARCHITECTURE%"=="AMD64" goto 64BIT
echo Removing 32bit service
%mypath%dbforbix32.exe //DS//DBforBIX
goto END

:64BIT
echo Removing 64bit service
%mypath%dbforbix64.exe //DS//DBforBIX

:END
pause