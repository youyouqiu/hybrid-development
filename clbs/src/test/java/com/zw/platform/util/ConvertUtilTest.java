package com.zw.platform.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConvertUtilTest {

    @Test
    public void binaryIntegerWithOne() {
        int a = 1 << 8;
        assertEquals(1, (int) ConvertUtil.binaryIntegerWithOne(a, 8));
        assertEquals(0, (int) ConvertUtil.binaryIntegerWithOne(a, 1));
    }

    @Test
    public void binaryLongWithOne() {
        long b = 1L << 40;
        assertEquals(1, (int) ConvertUtil.binaryLongWithOne(b, 40));
        assertEquals(0, (int) ConvertUtil.binaryLongWithOne(b, 41));
    }

    @Test
    public void toHexString() {
        byte[] bytes = new byte[] { 0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef };
        assertEquals("0123456789ABCDEF", ConvertUtil.toHexString(bytes));
    }
}
