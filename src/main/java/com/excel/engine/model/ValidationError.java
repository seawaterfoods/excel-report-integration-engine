package com.excel.engine.model;

/**
 * 驗證錯誤資訊。
 */
public class ValidationError {

    /** 錯誤類型 */
    private ErrorType errorType;

    /** 錯誤訊息 */
    private String message;

    /** 相關來源檔案 */
    private String sourceFile;

    /** 相關儲存格座標 (若適用) */
    private String coordinate;

    public enum ErrorType {
        FILE_COUNT_MISMATCH,
        INVALID_FILE_NAME,
        DUPLICATE_CELL,
        TEMPLATE_NOT_FOUND,
        DIRECTORY_NOT_FOUND,
        FILE_READ_ERROR,
        FILE_WRITE_ERROR
    }

    public ValidationError() {
    }

    public ValidationError(ErrorType errorType, String message, String sourceFile, String coordinate) {
        this.errorType = errorType;
        this.message = message;
        this.sourceFile = sourceFile;
        this.coordinate = coordinate;
    }

    public ErrorType getErrorType() { return errorType; }
    public void setErrorType(ErrorType errorType) { this.errorType = errorType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }

    public String getCoordinate() { return coordinate; }
    public void setCoordinate(String coordinate) { this.coordinate = coordinate; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ErrorType errorType;
        private String message;
        private String sourceFile;
        private String coordinate;

        public Builder errorType(ErrorType errorType) { this.errorType = errorType; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder sourceFile(String sourceFile) { this.sourceFile = sourceFile; return this; }
        public Builder coordinate(String coordinate) { this.coordinate = coordinate; return this; }

        public ValidationError build() {
            return new ValidationError(errorType, message, sourceFile, coordinate);
        }
    }
}
