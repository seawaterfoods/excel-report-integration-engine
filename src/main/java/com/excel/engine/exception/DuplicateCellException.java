package com.excel.engine.exception;

/**
 * 重複儲存格寫入例外。
 */
public class DuplicateCellException extends RuntimeException {

    private final String coordinate;
    private final String firstSource;
    private final String secondSource;

    public DuplicateCellException(String coordinate, String firstSource, String secondSource) {
        super(String.format("儲存格 %s 重複寫入：已由 [%s] 寫入，[%s] 嘗試再次寫入",
                coordinate, firstSource, secondSource));
        this.coordinate = coordinate;
        this.firstSource = firstSource;
        this.secondSource = secondSource;
    }

    public String getCoordinate() { return coordinate; }
    public String getFirstSource() { return firstSource; }
    public String getSecondSource() { return secondSource; }
}
