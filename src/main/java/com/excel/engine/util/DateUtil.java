package com.excel.engine.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private DateUtil() {}

    /**
     * 取得今日日期字串 (yyyyMMdd)。
     */
    public static String todayString() {
        return LocalDate.now().format(OUTPUT_DATE_FORMAT);
    }
}
