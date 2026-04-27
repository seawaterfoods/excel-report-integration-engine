package com.excel.engine.model;

import java.nio.file.Path;
import java.util.List;

/**
 * 單一報表的處理任務描述。
 */
public class ReportTask {

    /** 報表名稱 (目錄名) */
    private String reportName;

    /** 民國年 */
    private String year;

    /** 月份 */
    private String month;

    /** 樣板檔路徑 */
    private Path templatePath;

    /** 匯入檔清單 */
    private List<Path> importFiles;

    /** 輸出目錄 */
    private Path outputDir;

    /** 產出檔名 */
    private String outputFileName;

    /** scope 設定 (可為 null，代表使用預設 AUTO_DETECT) */
    private ScopeConfig scopeConfig;

    public ReportTask() {
    }

    public ReportTask(String reportName, String year, String month, Path templatePath,
                      List<Path> importFiles, Path outputDir, String outputFileName,
                      ScopeConfig scopeConfig) {
        this.reportName = reportName;
        this.year = year;
        this.month = month;
        this.templatePath = templatePath;
        this.importFiles = importFiles;
        this.outputDir = outputDir;
        this.outputFileName = outputFileName;
        this.scopeConfig = scopeConfig;
    }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public Path getTemplatePath() { return templatePath; }
    public void setTemplatePath(Path templatePath) { this.templatePath = templatePath; }

    public List<Path> getImportFiles() { return importFiles; }
    public void setImportFiles(List<Path> importFiles) { this.importFiles = importFiles; }

    public Path getOutputDir() { return outputDir; }
    public void setOutputDir(Path outputDir) { this.outputDir = outputDir; }

    public String getOutputFileName() { return outputFileName; }
    public void setOutputFileName(String outputFileName) { this.outputFileName = outputFileName; }

    public ScopeConfig getScopeConfig() { return scopeConfig; }
    public void setScopeConfig(ScopeConfig scopeConfig) { this.scopeConfig = scopeConfig; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String reportName;
        private String year;
        private String month;
        private Path templatePath;
        private List<Path> importFiles;
        private Path outputDir;
        private String outputFileName;
        private ScopeConfig scopeConfig;

        public Builder reportName(String reportName) { this.reportName = reportName; return this; }
        public Builder year(String year) { this.year = year; return this; }
        public Builder month(String month) { this.month = month; return this; }
        public Builder templatePath(Path templatePath) { this.templatePath = templatePath; return this; }
        public Builder importFiles(List<Path> importFiles) { this.importFiles = importFiles; return this; }
        public Builder outputDir(Path outputDir) { this.outputDir = outputDir; return this; }
        public Builder outputFileName(String outputFileName) { this.outputFileName = outputFileName; return this; }
        public Builder scopeConfig(ScopeConfig scopeConfig) { this.scopeConfig = scopeConfig; return this; }

        public ReportTask build() {
            return new ReportTask(reportName, year, month, templatePath, importFiles,
                    outputDir, outputFileName, scopeConfig);
        }
    }
}
