package com.excel.engine.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 單一報表的處理結果。
 */
public class ReportResult {

    /** 報表名稱 */
    private String reportName;

    /** 年 / 月 */
    private String year;
    private String month;

    /** 是否成功 */
    private boolean success;

    /** 處理檔案數 */
    private int filesProcessed;

    /** 寫入儲存格數 */
    private int cellsWritten;

    /** 產出檔名 */
    private String outputFileName;

    /** 錯誤清單 */
    private List<ValidationError> errors = new ArrayList<>();

    public ReportResult() {
    }

    public ReportResult(String reportName, String year, String month, boolean success,
                        int filesProcessed, int cellsWritten, String outputFileName,
                        List<ValidationError> errors) {
        this.reportName = reportName;
        this.year = year;
        this.month = month;
        this.success = success;
        this.filesProcessed = filesProcessed;
        this.cellsWritten = cellsWritten;
        this.outputFileName = outputFileName;
        this.errors = errors;
    }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getFilesProcessed() { return filesProcessed; }
    public void setFilesProcessed(int filesProcessed) { this.filesProcessed = filesProcessed; }

    public int getCellsWritten() { return cellsWritten; }
    public void setCellsWritten(int cellsWritten) { this.cellsWritten = cellsWritten; }

    public String getOutputFileName() { return outputFileName; }
    public void setOutputFileName(String outputFileName) { this.outputFileName = outputFileName; }

    public List<ValidationError> getErrors() { return errors; }
    public void setErrors(List<ValidationError> errors) { this.errors = errors; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String reportName;
        private String year;
        private String month;
        private boolean success;
        private int filesProcessed;
        private int cellsWritten;
        private String outputFileName;
        private List<ValidationError> errors = new ArrayList<>();

        public Builder reportName(String reportName) { this.reportName = reportName; return this; }
        public Builder year(String year) { this.year = year; return this; }
        public Builder month(String month) { this.month = month; return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder filesProcessed(int filesProcessed) { this.filesProcessed = filesProcessed; return this; }
        public Builder cellsWritten(int cellsWritten) { this.cellsWritten = cellsWritten; return this; }
        public Builder outputFileName(String outputFileName) { this.outputFileName = outputFileName; return this; }
        public Builder errors(List<ValidationError> errors) { this.errors = errors; return this; }

        public ReportResult build() {
            return new ReportResult(reportName, year, month, success, filesProcessed,
                    cellsWritten, outputFileName, errors);
        }
    }
}
