package com.excel.engine.service;

import com.excel.engine.model.CellData;
import com.excel.engine.model.CellData.CellValueType;
import com.excel.engine.model.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    void validateFileCount_matches() {
        List<Path> files = List.of(Path.of("01_a.xlsx"), Path.of("02_b.xlsx"));
        List<ValidationError> errors = validationService.validateFileCount(files, 2);
        assertTrue(errors.isEmpty());
    }

    @Test
    void validateFileCount_mismatch() {
        List<Path> files = List.of(Path.of("01_a.xlsx"));
        List<ValidationError> errors = validationService.validateFileCount(files, 3);
        assertEquals(1, errors.size());
        assertEquals(ValidationError.ErrorType.FILE_COUNT_MISMATCH, errors.get(0).getErrorType());
    }

    @Test
    void validateFileCount_nullExpected_skips() {
        List<Path> files = List.of(Path.of("01_a.xlsx"));
        List<ValidationError> errors = validationService.validateFileCount(files, null);
        assertTrue(errors.isEmpty());
    }

    @Test
    void validateFileNames_allValid() {
        List<Path> files = List.of(
                Path.of("01_報表.xlsx"),
                Path.of("99_test.xlsx")
        );
        List<ValidationError> errors = validationService.validateFileNames(files);
        assertTrue(errors.isEmpty());
    }

    @Test
    void validateFileNames_invalid() {
        List<Path> files = List.of(
                Path.of("01_報表.xlsx"),
                Path.of("AB_bad.xlsx")
        );
        List<ValidationError> errors = validationService.validateFileNames(files);
        assertEquals(1, errors.size());
        assertEquals(ValidationError.ErrorType.INVALID_FILE_NAME, errors.get(0).getErrorType());
    }

    @Test
    void validateNoDuplicates_noDup() {
        Map<String, String> tracker = new HashMap<>();
        tracker.put("B3", "01_報表.xlsx");

        CellData cell = new CellData();
        cell.setCoordinate("B4");
        cell.setValue("V");
        cell.setSourceFile("02_報表.xlsx");
        cell.setValueType(CellValueType.STRING);

        List<ValidationError> errors = validationService.validateNoDuplicates(tracker, List.of(cell));
        assertTrue(errors.isEmpty());
    }

    @Test
    void validateNoDuplicates_hasDup() {
        Map<String, String> tracker = new HashMap<>();
        tracker.put("B3", "01_報表.xlsx");

        CellData cell = new CellData();
        cell.setCoordinate("B3");
        cell.setValue("V");
        cell.setSourceFile("02_報表.xlsx");
        cell.setValueType(CellValueType.STRING);

        List<ValidationError> errors = validationService.validateNoDuplicates(tracker, List.of(cell));
        assertEquals(1, errors.size());
        assertEquals(ValidationError.ErrorType.DUPLICATE_CELL, errors.get(0).getErrorType());
        assertTrue(errors.get(0).getMessage().contains("B3"));
    }
}
