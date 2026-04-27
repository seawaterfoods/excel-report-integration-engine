package com.excel.engine.service;

import com.excel.engine.config.AppConfig;
import com.excel.engine.model.ReadMode;
import com.excel.engine.model.ReportTask;
import com.excel.engine.model.ScopeConfig;
import com.excel.engine.util.FileNameUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final AppConfig appConfig;

    public FileService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * 根據設定解析所有需要處理的目錄 (年/月/報表名)。
     * 回傳 ReportTask 清單。
     */
    public List<ReportTask> resolveReportTasks() throws IOException {
        Path importRoot = Path.of(appConfig.getImportDir()).toAbsolutePath().normalize();
        if (!Files.isDirectory(importRoot)) {
            log.error("匯入目錄不存在: {}", importRoot);
            return List.of();
        }

        List<String> yearDirs = resolveYears(importRoot);
        List<ReportTask> tasks = new ArrayList<>();

        for (String year : yearDirs) {
            List<String> monthDirs = resolveMonths(importRoot.resolve(year));
            for (String month : monthDirs) {
                Path monthPath = importRoot.resolve(year).resolve(month);
                List<ReportTask> monthTasks = resolveReportsInMonth(monthPath, year, month);
                tasks.addAll(monthTasks);
            }
        }

        log.info("共解析到 {} 個報表任務", tasks.size());
        return tasks;
    }

    private List<String> resolveYears(Path importRoot) throws IOException {
        String configYear = appConfig.getProcessYear();
        if (configYear != null && !configYear.isBlank()) {
            Path yearPath = importRoot.resolve(configYear.trim());
            if (Files.isDirectory(yearPath)) {
                return List.of(configYear.trim());
            }
            log.warn("指定年度目錄不存在: {}", yearPath);
            return List.of();
        }
        return listSubDirectories(importRoot);
    }

    private List<String> resolveMonths(Path yearPath) throws IOException {
        String configMonth = appConfig.getProcessMonth();
        if (configMonth != null && !configMonth.isBlank()) {
            Path monthPath = yearPath.resolve(configMonth.trim());
            if (Files.isDirectory(monthPath)) {
                return List.of(configMonth.trim());
            }
            log.warn("指定月份目錄不存在: {}", monthPath);
            return List.of();
        }
        return listSubDirectories(yearPath);
    }

    /**
     * 解析某月份目錄下的所有報表任務。
     * 支援兩種結構:
     *   1. monthDir/reportName/templet/XXX.xlsx + 匯入檔
     *   2. monthDir/templet/XXX.xlsx + 匯入檔 (向後相容，視為單一報表)
     */
    private List<ReportTask> resolveReportsInMonth(Path monthPath, String year, String month) throws IOException {
        List<ReportTask> tasks = new ArrayList<>();
        List<String> reportFilter = appConfig.getProcessReportList();

        // 檢查是否有子目錄作為報表名
        List<String> subDirs = listSubDirectories(monthPath);
        boolean hasReportSubDirs = subDirs.stream()
                .anyMatch(d -> !d.equalsIgnoreCase("templet"));

        if (hasReportSubDirs) {
            // 新結構: monthDir/reportName/...
            for (String dirName : subDirs) {
                if (dirName.equalsIgnoreCase("templet")) continue;
                if (!reportFilter.isEmpty() && !reportFilter.contains(dirName)) {
                    log.debug("跳過報表 (未在 process-reports 中): {}", dirName);
                    continue;
                }
                Path reportDir = monthPath.resolve(dirName);
                ReportTask task = buildReportTask(reportDir, dirName, year, month);
                if (task != null) tasks.add(task);
            }
        } else {
            // 舊結構: monthDir/templet/XXX.xlsx + 匯入檔
            Path templetDir = monthPath.resolve("templet");
            if (Files.isDirectory(templetDir)) {
                String reportName = detectReportName(templetDir);
                if (reportName != null) {
                    if (reportFilter.isEmpty() || reportFilter.contains(reportName)) {
                        ReportTask task = buildReportTask(monthPath, reportName, year, month);
                        if (task != null) tasks.add(task);
                    }
                }
            }
        }

        return tasks;
    }

    private ReportTask buildReportTask(Path reportDir, String reportName, String year, String month) throws IOException {
        Path templetDir = reportDir.resolve("templet");
        if (!Files.isDirectory(templetDir)) {
            log.warn("報表目錄缺少 templet 子目錄: {}", reportDir);
            return null;
        }

        Path templateFile = findTemplateFile(templetDir);
        if (templateFile == null) {
            log.warn("templet 目錄中找不到 xlsx 樣板檔: {}", templetDir);
            return null;
        }

        List<Path> importFiles = findImportFiles(reportDir);
        ScopeConfig scopeConfig = loadScopeConfig(templetDir);

        Path outputDir = Path.of(appConfig.getOutputDir()).toAbsolutePath().normalize()
                .resolve(year).resolve(month);

        String outputFileName = FileNameUtil.generateOutputFileName(templateFile.getFileName().toString());

        return ReportTask.builder()
                .reportName(reportName)
                .year(year)
                .month(month)
                .templatePath(templateFile)
                .importFiles(importFiles)
                .outputDir(outputDir)
                .outputFileName(outputFileName)
                .scopeConfig(scopeConfig)
                .build();
    }

    /**
     * 在 templet 目錄中找到第一個 .xlsx 檔。
     */
    public Path findTemplateFile(Path templetDir) throws IOException {
        try (Stream<Path> paths = Files.list(templetDir)) {
            return paths
                    .filter(p -> p.toString().toLowerCase().endsWith(".xlsx"))
                    .filter(p -> !p.getFileName().toString().startsWith("~"))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 找出報表目錄中的匯入檔 (排除 templet 子目錄)。
     */
    public List<Path> findImportFiles(Path reportDir) throws IOException {
        try (Stream<Path> paths = Files.list(reportDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".xlsx"))
                    .filter(p -> !p.getFileName().toString().startsWith("~"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    /**
     * 從 templet 目錄偵測報表名 (取 xlsx 檔名，不含副檔名)。
     */
    private String detectReportName(Path templetDir) throws IOException {
        Path template = findTemplateFile(templetDir);
        if (template == null) return null;
        String name = template.getFileName().toString();
        return name.substring(0, name.lastIndexOf('.'));
    }

    /**
     * 載入 scope.yaml (若存在)。
     */
    public ScopeConfig loadScopeConfig(Path templetDir) {
        Path scopeFile = templetDir.resolve("scope.yaml");
        if (!Files.exists(scopeFile)) {
            scopeFile = templetDir.resolve("scope.yml");
        }
        if (!Files.exists(scopeFile)) {
            return ScopeConfig.builder().readMode(ReadMode.AUTO_DETECT).build();
        }

        try {
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            ScopeConfig config = yamlMapper.readValue(scopeFile.toFile(), ScopeConfig.class);
            log.info("已載入 scope 設定: {}", scopeFile);
            return config;
        } catch (IOException e) {
            log.warn("讀取 scope 設定失敗，使用預設值: {}", e.getMessage());
            return ScopeConfig.builder().readMode(ReadMode.AUTO_DETECT).build();
        }
    }

    private List<String> listSubDirectories(Path parent) throws IOException {
        if (!Files.isDirectory(parent)) return List.of();
        try (Stream<Path> paths = Files.list(parent)) {
            return paths
                    .filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        }
    }
}
