package com.excel.engine.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileNameUtilTest {

    @Test
    void validImportFileName() {
        assertTrue(FileNameUtil.isValidImportFileName("01_報表.xlsx"));
        assertTrue(FileNameUtil.isValidImportFileName("99_test.xlsx"));
        assertTrue(FileNameUtil.isValidImportFileName("00_ABC_v1.xlsx"));
    }

    @Test
    void invalidImportFileName() {
        assertFalse(FileNameUtil.isValidImportFileName("AB_報表.xlsx"));
        assertFalse(FileNameUtil.isValidImportFileName("1_報表.xlsx"));
        assertFalse(FileNameUtil.isValidImportFileName("報表.xlsx"));
        assertFalse(FileNameUtil.isValidImportFileName("01_報表.xls"));
    }

    @Test
    void generateOutputFileName() {
        String result = FileNameUtil.generateOutputFileName("報表_v1.1.xlsx");
        // Should start with today's date (yyyyMMdd) and contain template name
        assertTrue(result.matches("\\d{8}_報表_v1\\.1\\.xlsx"));
    }
}
