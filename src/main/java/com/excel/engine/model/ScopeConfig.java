package com.excel.engine.model;

/**
 * 報表讀取範圍設定，對應 templet/scope.yaml。
 */
public class ScopeConfig {

    /** 讀取模式 */
    private ReadMode readMode = ReadMode.AUTO_DETECT;

    /** 起始儲存格 (area / horizontal / vertical 模式使用) */
    private String startCell;

    /** 結束儲存格 (horizontal / vertical 模式使用) */
    private String endCell;

    /** 預期匯入檔案數 (留空=不檢核) */
    private Integer expectedFileCount;

    public ScopeConfig() {
    }

    public ScopeConfig(ReadMode readMode, String startCell, String endCell, Integer expectedFileCount) {
        this.readMode = readMode;
        this.startCell = startCell;
        this.endCell = endCell;
        this.expectedFileCount = expectedFileCount;
    }

    public ReadMode getReadMode() { return readMode; }
    public void setReadMode(ReadMode readMode) { this.readMode = readMode; }

    public String getStartCell() { return startCell; }
    public void setStartCell(String startCell) { this.startCell = startCell; }

    public String getEndCell() { return endCell; }
    public void setEndCell(String endCell) { this.endCell = endCell; }

    public Integer getExpectedFileCount() { return expectedFileCount; }
    public void setExpectedFileCount(Integer expectedFileCount) { this.expectedFileCount = expectedFileCount; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ReadMode readMode = ReadMode.AUTO_DETECT;
        private String startCell;
        private String endCell;
        private Integer expectedFileCount;

        public Builder readMode(ReadMode readMode) { this.readMode = readMode; return this; }
        public Builder startCell(String startCell) { this.startCell = startCell; return this; }
        public Builder endCell(String endCell) { this.endCell = endCell; return this; }
        public Builder expectedFileCount(Integer expectedFileCount) { this.expectedFileCount = expectedFileCount; return this; }

        public ScopeConfig build() {
            return new ScopeConfig(readMode, startCell, endCell, expectedFileCount);
        }
    }
}
