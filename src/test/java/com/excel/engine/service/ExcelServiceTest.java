package com.excel.engine.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExcelServiceTest {

    @Test
    void parseCoordinate_simple() {
        int[] rc = ExcelService.parseCoordinate("A1");
        assertEquals(0, rc[0]); // row 0
        assertEquals(0, rc[1]); // col 0
    }

    @Test
    void parseCoordinate_B4() {
        int[] rc = ExcelService.parseCoordinate("B4");
        assertEquals(3, rc[0]); // row 3
        assertEquals(1, rc[1]); // col 1
    }

    @Test
    void parseCoordinate_H22() {
        int[] rc = ExcelService.parseCoordinate("H22");
        assertEquals(21, rc[0]); // row 21
        assertEquals(7, rc[1]);  // col 7
    }

    @Test
    void parseCoordinate_AA1() {
        int[] rc = ExcelService.parseCoordinate("AA1");
        assertEquals(0, rc[0]);  // row 0
        assertEquals(26, rc[1]); // col 26
    }

    @Test
    void parseCoordinate_Z100() {
        int[] rc = ExcelService.parseCoordinate("Z100");
        assertEquals(99, rc[0]); // row 99
        assertEquals(25, rc[1]); // col 25
    }
}
