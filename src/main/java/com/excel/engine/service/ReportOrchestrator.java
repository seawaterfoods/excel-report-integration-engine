package com.excel.engine.service;

import com.excel.engine.model.*;
import com.excel.engine.model.ValidationError.ErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class ReportOrchestrator implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ReportOrchestrator.class);

    private final FileService fileService;
    private final ExcelService excelService;
    private final ValidationService validationService;
    private final MergeService mergeService;

    public ReportOrchestrator(FileService fileService, ExcelService excelService,
                              ValidationService validationService, MergeService mergeService) {
        this.fileService = fileService;
        this.excelService = excelService;
        this.validationService = validationService;
        this.mergeService = mergeService;
    }

    @Override
    public void run(String... args) {
        log.info("========== Excel Report Integration Engine 啟動 ==========");
        ProcessResult result = execute();
        outputReport(result);

        if (result.isSuccess()) {
            log.info("========== 執行完成：全部成功 ==========");
        } else {
            log.error("========== 執行完成：部分失敗，請查看報告 ==========");
        }
    }

    /**
     * 主執行流程。
     */
    public ProcessResult execute() {
        LocalDateTime startTime = LocalDateTime.now();
        List<ReportResult> reportResults = new ArrayList<>();

        try {
            // 1. 解析所有報表任務
            List<ReportTask> tasks = fileService.resolveReportTasks();
            if (tasks.isEmpty()) {
                log.warn("未找到任何報表任務");
                return buildResult(startTime, reportResults, "未找到任何報表任務");
            }

            log.info("共找到 {} 個報表任務", tasks.size());

            // 2. 逐一處理每個報表 (各報表為獨立事件)
            for (ReportTask task : tasks) {
                ReportResult rr = processReport(task);
                reportResults.add(rr);
            }

        } catch (Exception e) {
            log.error("執行過程發生非預期錯誤: {}", e.getMessage(), e);
        }

        return buildResult(startTime, reportResults, null);
    }

    /**
     * 處理單一報表任務。
     */
    private ReportResult processReport(ReportTask task) {
        String reportLabel = String.format("[%s/%s] %s", task.getYear(), task.getMonth(), task.getReportName());
        log.info("--- 開始處理報表: {} ---", reportLabel);

        ReportResult.Builder resultBuilder = ReportResult.builder()
                .reportName(task.getReportName())
                .year(task.getYear())
                .month(task.getMonth())
                .outputFileName(task.getOutputFileName());

        List<ValidationError> allErrors = new ArrayList<>();

        try {
            List<Path> importFiles = task.getImportFiles();
            ScopeConfig scope = task.getScopeConfig();

            // 檢核 1: 檔案名稱格式
            List<ValidationError> nameErrors = validationService.validateFileNames(importFiles);
            allErrors.addAll(nameErrors);

            // 檢核 2: 檔案數量
            Integer expectedCount = (scope != null) ? scope.getExpectedFileCount() : null;
            List<ValidationError> countErrors = validationService.validateFileCount(importFiles, expectedCount);
            allErrors.addAll(countErrors);

            // 任何前置檢核失敗 → 中止此報表
            if (!allErrors.isEmpty()) {
                log.error("報表 {} 前置檢核失敗，跳過處理", reportLabel);
                return resultBuilder.success(false).errors(allErrors).build();
            }

            // 複製樣板至輸出
            Path outputPath = task.getOutputDir().resolve(task.getOutputFileName());
            excelService.copyTemplate(task.getTemplatePath(), outputPath);

            // 讀取樣板 cell 資料 (作為比對基準)
            String templateFileName = task.getTemplatePath().getFileName().toString();
            List<CellData> templateCells = excelService.readAllCellData(task.getTemplatePath(), templateFileName);
            log.info("樣板 cell 數: {}", templateCells.size());

            // 追蹤已寫入座標
            Map<String, String> writeTracker = new HashMap<>();
            int totalCellsWritten = 0;

            // 開啟產出檔工作簿
            try (Workbook outputWb = excelService.openWorkbook(outputPath)) {

                boolean hasDuplicates = false;

                // 逐檔處理 (全部掃描完畢後再判斷是否中止)
                for (Path importFile : importFiles) {
                    String importFileName = importFile.getFileName().toString();
                    log.info("處理匯入檔: {}", importFileName);

                    // 讀取匯入檔
                    List<CellData> importCells = excelService.readAllCellData(importFile, importFileName);

                    // 萃取獨有 cell
                    List<CellData> uniqueCells = mergeService.extractUniqueCells(importCells, templateCells);
                    log.info("  獨有 cell 數: {}", uniqueCells.size());

                    if (uniqueCells.isEmpty()) continue;

                    // 檢核 3: 重複座標
                    List<ValidationError> dupErrors = validationService.validateNoDuplicates(writeTracker, uniqueCells);
                    if (!dupErrors.isEmpty()) {
                        allErrors.addAll(dupErrors);
                        hasDuplicates = true;
                    }

                    // 僅在無重複時寫入產出檔
                    if (!hasDuplicates) {
                        excelService.writeCellData(outputWb, uniqueCells);
                        totalCellsWritten += uniqueCells.size();
                    }

                    // 更新追蹤器 (使用 putIfAbsent 保留首次寫入來源，以偵測後續所有重複)
                    for (CellData cd : uniqueCells) {
                        writeTracker.putIfAbsent(cd.getCoordinate(), cd.getSourceFile());
                    }
                }

                // 若有重複座標，刪除不完整輸出並回報所有錯誤
                if (hasDuplicates) {
                    log.error("報表 {} 偵測到重複座標 (共 {} 處)，中止匯出", reportLabel, allErrors.size());
                    Files.deleteIfExists(outputPath);
                    return resultBuilder
                            .success(false)
                            .filesProcessed(importFiles.size())
                            .errors(allErrors)
                            .build();
                }

                // 儲存產出檔
                excelService.saveWorkbook(outputWb, outputPath);
            }

            log.info("報表 {} 處理完成: {} 檔 / {} cells",
                    reportLabel, importFiles.size(), totalCellsWritten);

            return resultBuilder
                    .success(true)
                    .filesProcessed(importFiles.size())
                    .cellsWritten(totalCellsWritten)
                    .errors(allErrors)
                    .build();

        } catch (Exception e) {
            log.error("處理報表 {} 時發生錯誤: {}", reportLabel, e.getMessage(), e);
            allErrors.add(ValidationError.builder()
                    .errorType(ErrorType.FILE_READ_ERROR)
                    .message(e.getMessage())
                    .build());
            return resultBuilder.success(false).errors(allErrors).build();
        }
    }

    private ProcessResult buildResult(LocalDateTime startTime, List<ReportResult> results, String summary) {
        long successCount = results.stream().filter(ReportResult::isSuccess).count();
        long failureCount = results.size() - successCount;
        boolean allSuccess = failureCount == 0 && !results.isEmpty();

        if (summary == null) {
            summary = String.format("處理 %d 個報表: %d 成功, %d 失敗",
                    results.size(), successCount, failureCount);
        }

        return ProcessResult.builder()
                .executionTime(startTime)
                .success(allSuccess)
                .totalReports(results.size())
                .successCount((int) successCount)
                .failureCount((int) failureCount)
                .reportResults(results)
                .summary(summary)
                .build();
    }

    /**
     * 輸出 JSON 格式的執行報告。
     */
    private void outputReport(ProcessResult result) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String json = mapper.writeValueAsString(result);
            log.info("執行報告:\n{}", json);

            // 寫入檔案
            Path reportPath = Path.of("logs", "report.json");
            Files.createDirectories(reportPath.getParent());
            Files.writeString(reportPath, json);
            log.info("執行報告已寫入: {}", reportPath.toAbsolutePath());

        } catch (IOException e) {
            log.error("輸出執行報告失敗: {}", e.getMessage());
        }
    }
}
