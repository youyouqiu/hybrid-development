package com.zw.platform.util.common;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ZipUtilTest {

    @BeforeClass
    public static void setUp() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");
    }

    @Test
    public void gzip() {
        String source = "test";
        String target = "H4sIAAAAAAAAACtJLS4BAAx+f9gEAAAA";
        String compressed = ZipUtil.gzip(source);
        Assert.assertEquals(target, compressed);
        Assert.assertEquals(source, ZipUtil.gunzip(compressed));
    }

    @Test
    public void compress() {
        String source = "测试";
        byte[] target = new byte[] { 31, -117, 8, 0, 0, 0, 0, 0, 0, 0, 123, -74, -75, -5, -59, -6, -87, 0, -105, -87,
            -85, -99, 6, 0, 0, 0 };
        byte[] compressed = ZipUtil.compress(source).getBytes(StandardCharsets.ISO_8859_1);
        Assert.assertArrayEquals(target, compressed);
        Assert.assertEquals(source, ZipUtil.uncompress(compressed, "UTF-8"));
    }
}