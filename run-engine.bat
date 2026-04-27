@echo off
chcp 65001 >nul
echo ========================================
echo Excel Report Integration Engine - 執行輔助腳本
echo ========================================
echo.
SETLOCAL ENABLEDELAYEDEXPANSION
REM Resolve script directory and move there
SET SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"
echo.
REM If no parameter, show interactive menu for double-click use
if "%1"=="" (
  echo 選擇操作:
  echo  1) build (封裝)
  echo  2) test (執行測試)
  echo  3) run (封裝並執行)
  echo  4) docker (docker-compose up)
  echo  5) exit
  set /p "CHOICE=輸入選項號碼並按 Enter [1-5] (預設 3): "
  if "%CHOICE%"=="" set "CHOICE=3"
  if "%CHOICE%"=="1" ( call "%~f0" build & exit /b 0 )
  if "%CHOICE%"=="2" ( call "%~f0" test & exit /b 0 )
  if "%CHOICE%"=="3" ( call "%~f0" run & exit /b 0 )
  if "%CHOICE%"=="4" ( call "%~f0" docker & exit /b 0 )
  if "%CHOICE%"=="5" (
    echo 取消
    pause
    exit /b 0
  )
)
echo === Java 檢查 ===
java -version 2>nul
if %ERRORLEVEL% neq 0 (
  echo [WARN] Java 未在 PATH 中找到，嘗試自動偵測常見 JDK 安裝路徑...
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
      goto :found_java
    )
  )
  :found_java
  if defined FOUND_JAVA (
    echo 已偵測到 JDK: %JAVA_HOME%
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    echo Java 版本:
    java -version
  ) else (
    echo [WARN] 未能自動偵測到 JDK 路徑。
    set /p "JAVA_INPUT=請輸入 JDK 安裝目錄 (或按 Enter 取消): "
    if "%JAVA_INPUT%"=="" (
      echo 取消。請安裝 JDK 17+ 或設定 JAVA_HOME 後重試。
      pause
      exit /b 1
    )
    if exist "%JAVA_INPUT%\bin\java.exe" (
      set "JAVA_HOME=%JAVA_INPUT%"
      set "PATH=%JAVA_HOME%\bin;%PATH%"
      echo 已設定 JAVA_HOME=%JAVA_HOME%
      echo Java 版本:
      java -version
    ) else (
      echo 找不到 %JAVA_INPUT%\bin\java.exe，請確認路徑正確後重試。
      pause
      exit /b 1
    )
  )
) else (
  echo Java 可用。
)
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
