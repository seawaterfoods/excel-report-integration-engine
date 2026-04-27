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

## 文件

| 文件 | 說明 |
|------|------|
| [需求盤點與缺漏分析](docs/01-需求盤點與缺漏分析.md) | 原始需求分析 |
| [系統架構圖](docs/02-系統架構圖.md) | Mermaid 架構圖 |
| [邏輯設計圖](docs/03-邏輯設計圖.md) | 流程與 Class 圖 |
| [開發流程規劃](docs/04-開發流程與階段規劃.md) | 階段規劃 |
| [使用手冊](docs/05-使用手冊.md) | 完整使用說明 |
| [開發階段紀錄](docs/06-開發階段紀錄.md) | 開發歷程 |

