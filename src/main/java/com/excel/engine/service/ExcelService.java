package com.excel.engine.service;

import com.excel.engine.model.CellData;
import com.excel.engine.model.CellData.CellValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class ExcelService {

    private static final Logger log = LoggerFactory.getLogger(ExcelService.class);

    /**
     * 開啟 Excel 工作簿 (保留公式)。
     */
    public Workbook openWorkbook(Path filePath) throws IOException {
        try (InputStream is = Files.newInputStream(filePath)) {
            return new XSSFWorkbook(is);
        }
    }

    /**
     * 完整複製樣板檔至目標路徑 (保留所有工作表、格式、公式)。
     */
    public void copyTemplate(Path templatePath, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        Files.copy(templatePath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("已複製樣板至: {}", outputPath);
    }

    /**
     * 讀取工作表中所有非空儲存格的資料。
     * 僅讀取第一個工作表 (Index 0)。
     */
    public List<CellData> readAllCellData(Path filePath, String sourceFileName) throws IOException {
        List<CellData> cellDataList = new ArrayList<>();
        try (Workbook wb = openWorkbook(filePath)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    CellData data = extractCellData(cell, sourceFileName);
                    if (data != null) {
                        cellDataList.add(data);
                    }
                }
            }
        }
        return cellDataList;
    }

    /**
     * 將 CellData 清單寫入指定工作簿的第一個工作表。
     * 僅寫入值，不改變格式。
     */
    public void writeCellData(Workbook workbook, List<CellData> cellDataList) {
        Sheet sheet = workbook.getSheetAt(0);
        for (CellData data : cellDataList) {
            int[] rowCol = parseCoordinate(data.getCoordinate());
            Row row = sheet.getRow(rowCol[0]);
            if (row == null) {
                row = sheet.createRow(rowCol[0]);
            }
            Cell cell = row.getCell(rowCol[1]);
            if (cell == null) {
                cell = row.createCell(rowCol[1]);
            }
            setCellValue(cell, data);
        }
    }

    /**
     * 儲存工作簿至檔案。
     */
    public void saveWorkbook(Workbook workbook, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            workbook.write(os);
        }
        log.info("已儲存產出檔: {}", outputPath);
    }

    private CellData extractCellData(Cell cell, String sourceFile) {
        if (cell == null) return null;

        CellType cellType = cell.getCellType();
        // 跳過公式 — 我們只讀值，不複製公式
        if (cellType == CellType.FORMULA) {
            return extractFormulaCachedValue(cell, sourceFile);
        }

        String coordinate = cell.getAddress().formatAsString();
        switch (cellType) {
            case STRING:
                String strVal = cell.getStringCellValue();
                if (strVal == null || strVal.isEmpty()) return null;
                return CellData.builder()
                        .coordinate(coordinate)
                        .value(strVal)
                        .sourceFile(sourceFile)
                        .valueType(CellValueType.STRING)
                        .build();
            case NUMERIC:
                return CellData.builder()
                        .coordinate(coordinate)
                        .value(cell.getNumericCellValue())
                        .sourceFile(sourceFile)
                        .valueType(CellValueType.NUMERIC)
                        .build();
            case BOOLEAN:
                return CellData.builder()
                        .coordinate(coordinate)
                        .value(cell.getBooleanCellValue())
                        .sourceFile(sourceFile)
                        .valueType(CellValueType.BOOLEAN)
                        .build();
            case BLANK:
            case _NONE:
            case ERROR:
            default:
                return null;
        }
    }

    private CellData extractFormulaCachedValue(Cell cell, String sourceFile) {
        // 公式 cell 不做為「資料」讀取，保留原始公式
        return null;
    }

    private void setCellValue(Cell cell, CellData data) {
        if (data.getValue() == null) return;
        switch (data.getValueType()) {
            case STRING:
                cell.setCellValue((String) data.getValue());
                break;
            case NUMERIC:
                cell.setCellValue(((Number) data.getValue()).doubleValue());
                break;
            case BOOLEAN:
                cell.setCellValue((Boolean) data.getValue());
                break;
            default:
                cell.setCellValue(data.getValue().toString());
        }
    }

    /**
     * 解析儲存格座標 (如 "B4") 為 [row, col] (0-based)。
     */
    static int[] parseCoordinate(String coordinate) {
        int colIndex = 0;
        int i = 0;
        while (i < coordinate.length() && Character.isLetter(coordinate.charAt(i))) {
            colIndex = colIndex * 26 + (Character.toUpperCase(coordinate.charAt(i)) - 'A' + 1);
            i++;
        }
        int rowIndex = Integer.parseInt(coordinate.substring(i)) - 1;
        return new int[]{rowIndex, colIndex - 1};
    }
}
