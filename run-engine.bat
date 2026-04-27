@echo off
REM run-engine.bat - build/run/test helper for Excel Report Integration Engine
REM Usage: run-engine.bat [build|test|run|docker]

SETLOCAL ENABLEDELAYEDEXPANSION
REM Resolve script directory and move there
SET SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

REM Function: ensure mvn available. If not, download a local Maven into .maven folder and use it.
where mvn >nul 2>&1
IF ERRORLEVEL 1 (
  REM check project-local .maven
  if exist "%SCRIPT_DIR%.maven\apache-maven-3.9.6\bin\mvn.cmd" (
    set PATH=%SCRIPT_DIR%.maven\apache-maven-3.9.6\bin;%PATH%
  ) else (
    echo mvn not found. Attempting to download Apache Maven 3.9.6 to project-local folder (.maven)
    powershell -Command "Set-StrictMode -Version Latest; $out = '%SCRIPT_DIR%'.TrimEnd('\\') + '\\.maven'; if (-not (Test-Path $out)) { New-Item -ItemType Directory -Path $out | Out-Null }; $zip = Join-Path $out 'apache-maven-3.9.6-bin.zip'; if (-not (Test-Path $zip)) { Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile $zip -UseBasicParsing } ; Expand-Archive -LiteralPath $zip -DestinationPath $out -Force"
    IF ERRORLEVEL 1 (
      echo WARNING: Auto-download of Maven failed. If you have Maven installed, add it to PATH.
    ) ELSE (
      set PATH=%SCRIPT_DIR%.maven\apache-maven-3.9.6\bin;%PATH%
      echo Apache Maven downloaded to %SCRIPT_DIR%.maven\apache-maven-3.9.6 and will be used for this run.
    )
  )
) ELSE (
  REM mvn exists in PATH - use it
)
nif "%1"=="build" (
  echo ==== Building project ====
  mvn clean package -DskipTests
  exit /b %ERRORLEVEL%
)
nif "%1"=="test" (
  echo ==== Running tests ====
  mvn test
  exit /b %ERRORLEVEL%
)
nif "%1"=="run" (
  echo ==== Packaging and running ====
  mvn clean package -DskipTests
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
