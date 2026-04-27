@echo off
REM Maven Wrapper - Windows
setlocal
if defined MAVEN_HOME (
  "%MAVEN_HOME%\bin\mvn" %*
  exit /b %errorlevel%
)
if defined JAVA_HOME (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXE=java"
)
"%JAVA_EXE%" -jar "%~dp0\.mvn\wrapper\maven-wrapper.jar" %*
endlocal
exit /b %errorlevel%
