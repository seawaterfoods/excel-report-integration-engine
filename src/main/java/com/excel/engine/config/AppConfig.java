package com.excel.engine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    /** 來源檔案目錄 */
    private String importDir = "./import";

    /** 輸出目錄 */
    private String outputDir = "./output";

    /** 處理年度 (民國年)，留空=處理全部 */
    private String processYear;

    /** 處理月份，留空=處理全部 */
    private String processMonth;

    /** 處理報表名稱 (逗號分隔)，留空=處理全部 */
    private String processReports;

    public String getImportDir() { return importDir; }
    public void setImportDir(String importDir) { this.importDir = importDir; }

    public String getOutputDir() { return outputDir; }
    public void setOutputDir(String outputDir) { this.outputDir = outputDir; }

    public String getProcessYear() { return processYear; }
    public void setProcessYear(String processYear) { this.processYear = processYear; }

    public String getProcessMonth() { return processMonth; }
    public void setProcessMonth(String processMonth) { this.processMonth = processMonth; }

    public String getProcessReports() { return processReports; }
    public void setProcessReports(String processReports) { this.processReports = processReports; }

    /**
     * 取得要處理的報表名稱清單。
     */
    public List<String> getProcessReportList() {
        if (processReports == null || processReports.isBlank()) {
            return List.of();
        }
        return List.of(processReports.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
