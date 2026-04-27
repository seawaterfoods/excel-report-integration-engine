package com.excel.engine.exception;

import com.excel.engine.model.ValidationError;

import java.util.List;

/**
 * 檔案驗證失敗例外。
 */
public class FileValidationException extends RuntimeException {

    private final List<ValidationError> errors;

    public FileValidationException(String message, List<ValidationError> errors) {
        super(message);
        this.errors = errors;
    }

    public FileValidationException(String message) {
        super(message);
        this.errors = List.of();
    }

    public List<ValidationError> getErrors() { return errors; }
}
