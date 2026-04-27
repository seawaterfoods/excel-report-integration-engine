package com.excel.engine.service;

import com.excel.engine.model.CellData;
import com.excel.engine.model.ValidationError;
import com.excel.engine.model.ValidationError.ErrorType;
import com.excel.engine.util.FileNameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;

@Service
public class ValidationService {

    private static final Logger log = LoggerFactory.getLogger(ValidationService.class);

    /**
     * 檢核檔案數量。
     * @return 錯誤清單 (空=通過)
     */
    public List<ValidationError> validateFileCount(List<Path> importFiles, Integer expectedCount) {
        if (expectedCount == null) {
            log.debug("未設定預期檔案數，跳過數量檢核");
            return List.of();
        }

        int actual = importFiles.size();
        if (actual != expectedCount) {
            String msg = String.format("檔案數量不符：預期 %d 份，實際 %d 份", expectedCount, actual);
            log.error(msg);
            return List.of(ValidationError.builder()
                    .errorType(ErrorType.FILE_COUNT_MISMATCH)
                    .message(msg)
                    .build());
        }
        log.info("檔案數量檢核通過: {} 份", actual);
        return List.of();
    }

    /**
     * 檢核所有匯入檔名格式 (前兩碼必須為數字)。
     * @return 錯誤清單 (空=通過)
     */
    public List<ValidationError> validateFileNames(List<Path> importFiles) {
        List<ValidationError> errors = new ArrayList<>();
        for (Path file : importFiles) {
            String fileName = file.getFileName().toString();
            if (!FileNameUtil.isValidImportFileName(fileName)) {
                String msg = String.format("檔案名稱格式不符: %s (前兩碼需為數字)", fileName);
                log.error(msg);
                errors.add(ValidationError.builder()
                        .errorType(ErrorType.INVALID_FILE_NAME)
                        .message(msg)
                        .sourceFile(fileName)
                        .build());
            }
        }
        if (errors.isEmpty()) {
            log.info("檔案名稱格式檢核通過");
        }
        return errors;
    }

    /**
     * 檢核新 cell 是否與已寫入的座標重複。
     * @param tracker     已寫入座標 → 來源檔 的對照表
     * @param newCells    本次要寫入的 cell 清單
     * @return 錯誤清單 (空=通過)
     */
    public List<ValidationError> validateNoDuplicates(
            Map<String, String> tracker, List<CellData> newCells) {

        List<ValidationError> errors = new ArrayList<>();
        for (CellData cell : newCells) {
            String existingSource = tracker.get(cell.getCoordinate());
            if (existingSource != null) {
                String msg = String.format("儲存格 %s 重複寫入：已由 [%s] 寫入，[%s] 嘗試再次寫入",
                        cell.getCoordinate(), existingSource, cell.getSourceFile());
                log.error(msg);
                errors.add(ValidationError.builder()
                        .errorType(ErrorType.DUPLICATE_CELL)
                        .message(msg)
                        .sourceFile(cell.getSourceFile())
                        .coordinate(cell.getCoordinate())
                        .build());
            }
        }
        return errors;
    }
}
