@echo off
REM run-engine.bat - build/run/test helper for Excel Report Integration Engine
REM Usage: run-engine.bat [build|test|run|docker]

SETLOCAL ENABLEDELAYEDEXPANSION
REM Resolve script directory and move there
SET SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

REM Ensure mvn available. Prefer project Maven Wrapper (mvnw.cmd) if present
if exist "%SCRIPT_DIR%mvnw.cmd" (
  set "MVN_CMD=%SCRIPT_DIR%mvnw.cmd"
) else (
  where mvn >nul 2>&1
  IF ERRORLEVEL 1 (
    echo ERROR: No mvn in PATH and mvnw.cmd not found. Please run mvnw.cmd or install Maven and add to PATH.
    exit /b 1
  ) ELSE (
    set "MVN_CMD=mvn"
  )
)
nif "%1"=="build" (
  echo ==== Building project ====
  %MVN_CMD% clean package -DskipTests
  exit /b %ERRORLEVEL%
)
nif "%1"=="test" (
  echo ==== Running tests ====
  %MVN_CMD% test
  exit /b %ERRORLEVEL%
)
nif "%1"=="run" (
  echo ==== Packaging and running ====
  %MVN_CMD% clean package -DskipTests
  if ERRORLEVEL 1 (
    echo Build failed. Aborting run.
    exit /b %ERRORLEVEL%
  )
  set JAR="%SCRIPT_DIR%target\excel-report-integration-engine-1.0.0-SNAPSHOT.jar"
  if not exist %JAR% (
    echo ERROR: jar not found: %JAR%
    exit /b 1
  )
  echo Running using config: %SCRIPT_DIR%config\application.yml
  java -jar %JAR% --spring.config.location=file:%SCRIPT_DIR%config\application.yml
  exit /b %ERRORLEVEL%
)
nif "%1"=="docker" (
  echo ==== Docker compose up (build) ====
  docker-compose up --build
  exit /b %ERRORLEVEL%
)
necho Usage: %~n0 [build^|test^|run^|docker]
echo  - build : mvn clean package -DskipTests
echo  - test  : mvn test
echo  - run   : package and run jar with config/application.yml
echo  - docker: docker-compose up --build
exit /b 1
