# Excel Report Integration Engine

Excel 數據自動化整合工具 — 自動讀取多份匯入檔，合併至樣板產出報表。

## 功能特色

- 🔄 自動比對樣板與匯入檔，萃取各公司獨有資料
- 📂 支援多年/月/報表批次處理
- ✅ 完整檢核：數量、檔名格式、重複座標偵測
- 📊 保留樣板所有格式、合併儲存格與公式
- 📋 JSON 執行報告 (整體 + 各報表獨立)
- 🐳 支援本地與 Docker 兩種執行方式

## 技術堆疊

| 項目 | 版本 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.4.5 |
| Apache POI | 5.3.0 |
| Maven | 3.9+ |

## 快速開始

### 本地執行

```bash
mvn clean package -DskipTests
java -jar target/excel-report-integration-engine-1.0.0-SNAPSHOT.jar \
  --spring.config.location=file:./config/application.yml
```

### Docker 執行

```bash
docker-compose up
```

## 目錄結構

```
import/{年}/{月}/{報表名}/
├── templet/
│   ├── 報表.xlsx              # 樣板
│   └── scope.yaml             # 讀取設定 (選用)
├── 01_報表.xlsx               # 匯入檔
├── 02_報表.xlsx
└── ...

output/{年}/{月}/
└── 20260427_報表.xlsx          # 產出
```

## 設定 (`config/application.yml`)

```yaml
app:
  import-dir: ./import
  output-dir: ./output
  process-year:                # 民國年 (留空=全部)
  process-month:               # 月份 (留空=全部)
  process-reports:             # 報表名，逗號分隔 (留空=全部)
```

## 使用 mvnw（Maven Wrapper）
專案已包含 Maven Wrapper (`mvnw` / `mvnw.cmd` 以及 `.mvn/wrapper`)：
- 建議使用 `mvnw`（Unix/macOS）或 `mvnw.cmd`（Windows），可在沒有全域安裝 Maven 時自動下載對應 Maven 版本。
- Windows 範例：在專案根目錄執行：
  ```bat
  mvnw.cmd clean package -DskipTests
  mvnw.cmd test
  mvnw.cmd spring-boot:run
  ```
- Unix 範例：
  ```bash
  ./mvnw clean package -DskipTests
  ./mvnw test
  ./mvnw spring-boot:run
  ```

如果希望使用系統已安裝的 Maven，請確保 `mvn` 可於 PATH 中存取。

## run-engine.bat 說明
本專案提供 `run-engine.bat` 作為快速執行輔助：
- 會優先使用專案中的 `mvnw.cmd`（若存在），否則使用 PATH 中的 `mvn`。
- 若兩者皆不存在，腳本會顯示錯誤並退出，請安裝 Maven 或執行 `mvnw.cmd`。

使用範例：
```bat
run-engine.bat build   :: 封裝 (會使用 mvnw 或 mvn)
run-engine.bat test    :: 執行測試
run-engine.bat run     :: 封裝並執行 jar
run-engine.bat docker  :: docker-compose up --build
```

## 文件

| 文件 | 說明 |
|------|------|
| [需求盤點與缺漏分析](docs/01-需求盤點與缺漏分析.md) | 原始需求分析 |
| [系統架構圖](docs/02-系統架構圖.md) | Mermaid 架構圖 |
| [邏輯設計圖](docs/03-邏輯設計圖.md) | 流程與 Class 圖 |
| [開發流程規劃](docs/04-開發流程與階段規劃.md) | 階段規劃 |
| [使用手冊](docs/05-使用手冊.md) | 完整使用說明 |
| [開發階段紀錄](docs/06-開發階段紀錄.md) | 開發歷程 |

