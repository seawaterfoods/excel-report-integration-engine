@echo off
chcp 65001 >nul

echo ========================================
echo   自留保費統計表報表轉換系統
echo   自動執行 Build + Run + Log解析
echo ========================================
echo.

REM ========================================
REM Step 0. Java 設定
REM ========================================
set "JAVA_EXE=C:\Users\user\.jdks\corretto-17.0.18\bin\java.exe"

if not exist "%JAVA_EXE%" (
    echo [錯誤] 找不到 Java：
    echo %JAVA_EXE%
    pause
    exit /b 1
)

for %%i in ("%JAVA_EXE%") do set "JAVA_HOME=%%~dpi"
set "JAVA_HOME=%JAVA_HOME:~0,-1%"
for %%i in ("%JAVA_HOME%") do set "JAVA_HOME=%%~dpi"
set "JAVA_HOME=%JAVA_HOME:~0,-1%"

set "PATH=%JAVA_HOME%\bin;%PATH%"

echo 使用 Java：
"%JAVA_EXE%" -version
echo.

REM ========================================
REM Step 1. Build
REM ========================================
echo [Step 1] 編譯中...
call mvnw.cmd clean package -DskipTests -q

if %ERRORLEVEL% neq 0 (
    echo ❌ 編譯失敗，停止執行
    pause
    exit /b 1
)

echo ✅ 編譯成功！
echo.

REM ========================================
REM Step 2. Run
REM ========================================
echo [Step 2] 執行程式...

set "JAR_FILE=target\excel-report-integration-engine-1.0.0-SNAPSHOT.jar"

if not exist "%JAR_FILE%" (
    echo [錯誤] 找不到 JAR：
    echo %JAR_FILE%
    pause
    exit /b 1
)

echo 開始時間：%date% %time%
echo.

"%JAVA_EXE%" -Dfile.encoding=UTF-8 -jar "%JAR_FILE%" ^
 --spring.config.additional-location=file:./config/ ^
 --logging.level.root=INFO ^
 > run.log 2>&1

echo.
echo 結束時間：%date% %time%
echo.

REM ========================================
REM Step 3. 檢查 output
REM ========================================
if exist output (
    echo ✅ 已產生 output 資料夾
) else (
    echo ⚠️ 沒看到 output，請查看 run.log
)

if exist run.log (
    echo 📄 log 檔：run.log
)

echo.

REM ========================================
REM Step 4. 只解析真正 ERROR（修正版）
REM ========================================
echo ===== 所有 ERROR 詳細內容 =====

set "REPORT_LOG=logs\report.log"

if exist "%REPORT_LOG%" (
    echo 發現 report.log，開始解析...
    echo.

    setlocal enabledelayedexpansion
    set "foundError="

    for /f "usebackq delims=" %%L in ("%REPORT_LOG%") do (
        set "line=%%L"

        REM ✅ 只抓獨立 ERROR（避免 errors 被誤判）
        echo !line! | findstr /i /c:" ERROR " >nul
        if !errorlevel! == 0 (
            set "foundError=1"
            echo ----------------------------------------
            echo !line!
        )
    )

    echo ----------------------------------------

    if defined foundError (
        echo ❌ 執行完成，但有發現 ERROR
    ) else (
        echo ✅ 正確執行完畢，無發現 ERROR
    )

    endlocal
) else (
    echo ⚠️ 找不到 report.log：
    echo %REPORT_LOG%
)

echo.
pause