package com.excel.engine.model;

/**
 * 單一儲存格資料，記錄座標、值與來源檔案。
 */
public class CellData {

    /** 儲存格座標，例如 "B4" */
    private String coordinate;

    /** 儲存格的值 (String / Number / Boolean / null) */
    private Object value;

    /** 來源檔案名稱 */
    private String sourceFile;

    /** 值的類型 (用於還原寫入時的 Cell Type) */
    private CellValueType valueType;

    public enum CellValueType {
        STRING, NUMERIC, BOOLEAN, BLANK
    }

    public CellData() {
    }

    public CellData(String coordinate, Object value, String sourceFile, CellValueType valueType) {
        this.coordinate = coordinate;
        this.value = value;
        this.sourceFile = sourceFile;
        this.valueType = valueType;
    }

    public String getCoordinate() { return coordinate; }
    public void setCoordinate(String coordinate) { this.coordinate = coordinate; }

    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }

    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }

    public CellValueType getValueType() { return valueType; }
    public void setValueType(CellValueType valueType) { this.valueType = valueType; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String coordinate;
        private Object value;
        private String sourceFile;
        private CellValueType valueType;

        public Builder coordinate(String coordinate) { this.coordinate = coordinate; return this; }
        public Builder value(Object value) { this.value = value; return this; }
        public Builder sourceFile(String sourceFile) { this.sourceFile = sourceFile; return this; }
        public Builder valueType(CellValueType valueType) { this.valueType = valueType; return this; }

        public CellData build() {
            return new CellData(coordinate, value, sourceFile, valueType);
        }
    }
}
