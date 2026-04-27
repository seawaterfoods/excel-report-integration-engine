@echo off
chcp 65001 >nul
echo ========================================
echo Excel Report Integration Engine - helper
echo ========================================
echo.
SETLOCAL ENABLEDELAYEDEXPANSION
REM Resolve script directory and move there
SET SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"
echo.
REM Interactive menu when no parameter (safe parsing)
if "%1"=="" (
  echo Choose action:
  echo  1) build (package)
  echo  2) test
  echo  3) run (package and run)
  echo  4) docker
  echo  5) exit
  set /p "CHOICE=Enter number [1-5] (default 3): "
  if "%CHOICE%"=="" set "CHOICE=3"
  if "%CHOICE%"=="1" set "CMD=build"
  if "%CHOICE%"=="2" set "CMD=test"
  if "%CHOICE%"=="3" set "CMD=run"
  if "%CHOICE%"=="4" set "CMD=docker"
  if "%CHOICE%"=="5" (
    echo Cancel
    pause
    exit /b 0
  )
)

REM Map selected CMD to ARG (used below). If script launched with parameter, use that instead.
if defined CMD (
  set "ARG=%CMD%"
) else (
  set "ARG=%~1"
)
echo Checking Java...
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
  echo [WARN] Java not found in PATH. Attempting common locations...
  set "FOUND_JAVA="
  for %%D in (
    "%ProgramFiles%\Java\jdk*"
    "%ProgramFiles(x86)%\Java\jdk*"
    "C:\Users\%USERNAME%\.jdks\*"
    "C:\Program Files\Amazon Corretto\jdk*"
    "C:\Program Files\Eclipse Adoptium\jdk*"
    "C:\Program Files\AdoptOpenJDK\jdk*"
    "C:\Program Files\Zulu\zulu*"
  ) do (
    if exist "%%~D\bin\java.exe" (
      set "JAVA_HOME=%%~D"
      set "FOUND_JAVA=1"
    )
  )
  if defined FOUND_JAVA (
    echo Detected JDK: %JAVA_HOME%
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    java -version
  ) else (
    set /p "JAVA_INPUT=Enter JDK install path (or press Enter to cancel): "
    if "%JAVA_INPUT%"=="" (
      echo Cancelled. Please install JDK 17+ or set JAVA_HOME.
      pause
      exit /b 1
    )
    if exist "%JAVA_INPUT%\bin\java.exe" (
      set "JAVA_HOME=%JAVA_INPUT%"
      set "PATH=%JAVA_HOME%\bin;%PATH%"
      echo JAVA_HOME set to %JAVA_HOME%
      java -version
    ) else (
      echo Could not find %JAVA_INPUT%\bin\java.exe
      pause
      exit /b 1
    )
  )
) else (
  echo Java available.
)

REM Ensure mvn available. Prefer project Maven Wrapper (mvnw.cmd) if present
if exist "%SCRIPT_DIR%mvnw.cmd" (
  set "MVN_CMD=%SCRIPT_DIR%mvnw.cmd"
) else (
  where mvn >nul 2>&1
  IF ERRORLEVEL 1 (
    echo ERROR: mvn or mvnw.cmd not found. Install Maven or use mvnw.cmd.
    pause
    exit /b 1
  ) ELSE (
    set "MVN_CMD=mvn"
  )
)
echo Using: %MVN_CMD%
echo.
if "%ARG%"=="build" (
  echo ==== Building project ====
  %MVN_CMD% clean package -DskipTests
  if ERRORLEVEL 1 (
    echo [ERROR] build failed
    pause
    exit /b %ERRORLEVEL%
  )
  echo build completed
  pause
  exit /b 0
)
if "%ARG%"=="test" (
  echo ==== Running tests ====
  %MVN_CMD% test
  if ERRORLEVEL 1 (
    echo [ERROR] tests failed
    pause
    exit /b %ERRORLEVEL%
  )
  echo tests completed
  pause
  exit /b 0
)
if "%ARG%"=="run" (
  echo ==== Packaging and running ====
  %MVN_CMD% clean package -DskipTests
  if ERRORLEVEL 1 (
    echo Build failed. Aborting run.
    pause
    exit /b %ERRORLEVEL%
  )
  set "JAR=%SCRIPT_DIR%target\excel-report-integration-engine-1.0.0-SNAPSHOT.jar"
  if not exist %JAR% (
    echo ERROR: jar not found: %JAR%
    pause
    exit /b 1
  )
  echo Running using config: %SCRIPT_DIR%config\application.yml
  java -jar %JAR% --spring.config.location=file:%SCRIPT_DIR%config\application.yml
  echo.
  echo ==== Run finished ====
  pause
  exit /b %ERRORLEVEL%
)
if "%ARG%"=="docker" (
  echo ==== Docker compose up (build) ====
  docker-compose up --build
  pause
  exit /b %ERRORLEVEL%
)
echo Usage: %~n0 [build^|test^|run^|docker]
echo  - build : %MVN_CMD% clean package -DskipTests
echo  - test  : %MVN_CMD% test
echo  - run   : package and run jar with config/application.yml
echo  - docker: docker-compose up --build
pause
