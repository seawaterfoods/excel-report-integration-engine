package com.excel.engine.service;

import com.excel.engine.model.CellData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MergeService {

    private static final Logger log = LoggerFactory.getLogger(MergeService.class);

    /**
     * 萃取匯入檔中的獨有資料 (與樣板相比的差異)。
     * 比對邏輯：匯入檔中有值但樣板中沒有 (或值不同) 的 cell。
     */
    public List<CellData> extractUniqueCells(
            List<CellData> importData, List<CellData> templateData) {

        // 建立樣板的座標→值 map
        Map<String, Object> templateMap = templateData.stream()
                .collect(Collectors.toMap(
                        CellData::getCoordinate,
                        cd -> cd.getValue() != null ? cd.getValue() : "",
                        (a, b) -> a));

        // 找出匯入檔中獨有的 cell
        List<CellData> uniqueCells = new ArrayList<>();
        for (CellData importCell : importData) {
            Object templateValue = templateMap.get(importCell.getCoordinate());
            if (templateValue == null) {
                // 樣板沒有此 cell → 獨有資料
                uniqueCells.add(importCell);
            } else if (!templateValue.equals(importCell.getValue())) {
                // 樣板有此 cell 但值不同 → 也是該公司的獨有資料
                uniqueCells.add(importCell);
            }
            // 值相同 → 共用資料，跳過
        }

        log.debug("萃取獨有 cell 數: {} (匯入 {} / 樣板 {})",
                uniqueCells.size(), importData.size(), templateData.size());
        return uniqueCells;
    }
}
