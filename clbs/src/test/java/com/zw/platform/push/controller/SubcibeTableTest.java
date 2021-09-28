package com.zw.platform.push.controller;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class SubcibeTableTest {

    @BeforeClass
    public static void setUp() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");
    }

    @Test
    public void put() throws InterruptedException {
        SubcibeTable.put("test", "1", 9);
        assertTrue(SubcibeTable.containsKey("test"));
        assertEquals("1", SubcibeTable.get("test"));
        Thread.sleep(10_000);
        assertFalse(SubcibeTable.containsKey("test"));
    }

    @Test
    public void clear() {
        SubcibeTable.put("test1", "1", 10);
        SubcibeTable.put("test2", "2", 10);
        assertEquals("1", SubcibeTable.get("test1"));
        assertEquals("2", SubcibeTable.get("test2"));
        SubcibeTable.clear();
        assertFalse(SubcibeTable.containsKey("test1"));
        assertFalse(SubcibeTable.containsKey("test2"));
    }

    @Test
    public void remove() {
        SubcibeTable.put("test", "1", 10);
        assertEquals("1", SubcibeTable.get("test"));
        SubcibeTable.remove("test");
        assertFalse(SubcibeTable.containsKey("test"));
    }
}
