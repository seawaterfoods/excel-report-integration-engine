package com.excel.engine.service;

import com.excel.engine.model.CellData;
import com.excel.engine.model.CellData.CellValueType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MergeServiceTest {

    private MergeService mergeService;

    @BeforeEach
    void setUp() {
        mergeService = new MergeService();
    }

    @Test
    void extractUniqueCells_findsNewCells() {
        List<CellData> templateData = List.of(
                makeCellData("A1", "公司名稱", "template"),
                makeCellData("B1", "欄位A", "template")
        );

        List<CellData> importData = List.of(
                makeCellData("A1", "公司名稱", "01_file.xlsx"),
                makeCellData("B1", "欄位A", "01_file.xlsx"),
                makeCellData("B3", "V", "01_file.xlsx")
        );

        List<CellData> unique = mergeService.extractUniqueCells(importData, templateData);
        assertEquals(1, unique.size());
        assertEquals("B3", unique.get(0).getCoordinate());
        assertEquals("V", unique.get(0).getValue());
    }

    @Test
    void extractUniqueCells_skipsIdenticalCells() {
        List<CellData> templateData = List.of(
                makeCellData("A1", "test", "template")
        );
        List<CellData> importData = List.of(
                makeCellData("A1", "test", "file.xlsx")
        );

        List<CellData> unique = mergeService.extractUniqueCells(importData, templateData);
        assertTrue(unique.isEmpty());
    }

    @Test
    void extractUniqueCells_detectsDifferentValues() {
        List<CellData> templateData = List.of(
                makeCellData("A1", "原始值", "template")
        );
        List<CellData> importData = List.of(
                makeCellData("A1", "新值", "file.xlsx")
        );

        List<CellData> unique = mergeService.extractUniqueCells(importData, templateData);
        assertEquals(1, unique.size());
    }

    @Test
    void extractUniqueCells_emptyImport() {
        List<CellData> templateData = List.of(
                makeCellData("A1", "test", "template")
        );

        List<CellData> unique = mergeService.extractUniqueCells(List.of(), templateData);
        assertTrue(unique.isEmpty());
    }

    private CellData makeCellData(String coord, String value, String source) {
        CellData cd = new CellData();
        cd.setCoordinate(coord);
        cd.setValue(value);
        cd.setSourceFile(source);
        cd.setValueType(CellValueType.STRING);
        return cd;
    }
}
