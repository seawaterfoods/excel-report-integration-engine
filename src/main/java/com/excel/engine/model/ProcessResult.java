package com.excel.engine.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 整體執行結果 (包含多個報表)。
 */
public class ProcessResult {

    /** 執行時間 */
    private LocalDateTime executionTime;

    /** 整體是否成功 */
    private boolean success;

    /** 總報表數 */
    private int totalReports;

    /** 成功報表數 */
    private int successCount;

    /** 失敗報表數 */
    private int failureCount;

    /** 各報表處理結果 */
    private List<ReportResult> reportResults = new ArrayList<>();

    /** 整體摘要訊息 */
    private String summary;

    public ProcessResult() {
    }

    public ProcessResult(LocalDateTime executionTime, boolean success, int totalReports,
                         int successCount, int failureCount, List<ReportResult> reportResults,
                         String summary) {
        this.executionTime = executionTime;
        this.success = success;
        this.totalReports = totalReports;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.reportResults = reportResults;
        this.summary = summary;
    }

    public LocalDateTime getExecutionTime() { return executionTime; }
    public void setExecutionTime(LocalDateTime executionTime) { this.executionTime = executionTime; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getTotalReports() { return totalReports; }
    public void setTotalReports(int totalReports) { this.totalReports = totalReports; }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }

    public List<ReportResult> getReportResults() { return reportResults; }
    public void setReportResults(List<ReportResult> reportResults) { this.reportResults = reportResults; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDateTime executionTime;
        private boolean success;
        private int totalReports;
        private int successCount;
        private int failureCount;
        private List<ReportResult> reportResults = new ArrayList<>();
        private String summary;

        public Builder executionTime(LocalDateTime executionTime) { this.executionTime = executionTime; return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder totalReports(int totalReports) { this.totalReports = totalReports; return this; }
        public Builder successCount(int successCount) { this.successCount = successCount; return this; }
        public Builder failureCount(int failureCount) { this.failureCount = failureCount; return this; }
        public Builder reportResults(List<ReportResult> reportResults) { this.reportResults = reportResults; return this; }
        public Builder summary(String summary) { this.summary = summary; return this; }

        public ProcessResult build() {
            return new ProcessResult(executionTime, success, totalReports, successCount,
                    failureCount, reportResults, summary);
        }
    }
}
