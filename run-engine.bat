@echo off
chcp 65001 >nul
echo ========================================
echo  Excel Report Integration Engine - 執行輔助腳本
echo ========================================
echo.
SETLOCAL ENABLEDELAYEDEXPANSION
REM Resolve script directory and move there
SET SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"
echo Java version:
java -version 2>&1 || echo [WARN] java 不可用，請確認已安裝 JDK 17+
echo.
REM Ensure mvn available. Prefer project Maven Wrapper (mvnw.cmd) if present
if exist "%SCRIPT_DIR%mvnw.cmd" (
  set "MVN_CMD=%SCRIPT_DIR%mvnw.cmd"
) else (
  where mvn >nul 2>&1
  IF ERRORLEVEL 1 (
    echo ERROR: 未找到 mvn 或 mvnw.cmd。請安裝 Maven 或使用專案內的 mvnw.cmd。
    pause
    exit /b 1
  ) ELSE (
    set "MVN_CMD=mvn"
  )
)
echo Using: %MVN_CMD%
echo.
if "%1"=="build" (
  echo ==== Building project ====
  %MVN_CMD% clean package -DskipTests
  if ERRORLEVEL 1 (
    echo [ERROR] build 失敗
    pause
    exit /b %ERRORLEVEL%
  )
  echo build 完成
  pause
  exit /b 0
)
if "%1"=="test" (
  echo ==== Running tests ====
  %MVN_CMD% test
  if ERRORLEVEL 1 (
    echo [ERROR] 測試失敗
    pause
    exit /b %ERRORLEVEL%
  )
  echo 測試完成
  pause
  exit /b 0
)
if "%1"=="run" (
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
if "%1"=="docker" (
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
