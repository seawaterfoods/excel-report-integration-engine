package com.excel.engine.util;

import java.util.regex.Pattern;

public final class FileNameUtil {

    private static final Pattern IMPORT_FILE_PATTERN = Pattern.compile("^\\d{2}_.+\\.xlsx$");

    private FileNameUtil() {}

    /**
     * 檢查匯入檔名是否符合格式 (前兩碼為數字)。
     */
    public static boolean isValidImportFileName(String fileName) {
        return IMPORT_FILE_PATTERN.matcher(fileName).matches();
    }

    /**
     * 產生輸出檔名: [yyyyMMdd]_[templateName]
     */
    public static String generateOutputFileName(String templateFileName) {
        return DateUtil.todayString() + "_" + templateFileName;
    }
}
