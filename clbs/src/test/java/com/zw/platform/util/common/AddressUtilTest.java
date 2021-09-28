package com.zw.platform.util.common;

import com.zw.platform.domain.oil.PositionInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AddressUtilTest {

    private static final String unknownLocation = "未获取到位置信息";
    private static final String unLocated = "未定位";

    // @BeforeClass
    public static void setUp() {
        AddressUtil addressUtil = new AddressUtil();
        addressUtil.setKey("c9902a4e6876b4ecf564198c4519b2b6");
        addressUtil.setUnknownLocation(unknownLocation);
        addressUtil.setNoLocation(unLocated);
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");
    }

    // @Test
    public void testResolveAddress() {
        PositionInfo info = AddressUtil.inverseAddress("116.481488", "39.990464");
        assertTrue(info.getFormattedAddress().startsWith("北京市朝阳区望京街道"));
        assertEquals("北京市", info.getCity());
        assertEquals("北京市", info.getProvince());
        assertEquals("010", info.getCityCode());
        assertEquals("朝阳区", info.getDistrict());
        assertEquals("110105", info.getAdcode());
        assertEquals("望京街道", info.getTownship());
        assertEquals("110105026000", info.getTownCode());
        assertEquals("阜通东大街", info.getStreet());
        assertEquals("6号", info.getStreetNumber());
    }

    // @Test
    public void testUnknownLocation() {
        PositionInfo info = AddressUtil.inverseAddress("0.0", "0.0");
        assertEquals(unLocated, info.getFormattedAddress());
        assertNull(info.getCity());

        info = AddressUtil.inverseAddress("0", "0.0");
        assertEquals(unLocated, info.getFormattedAddress());
        assertNull(info.getCity());

        info = AddressUtil.inverseAddress("0", "0");
        assertEquals(unLocated, info.getFormattedAddress());
        assertNull(info.getCity());

        info = AddressUtil.inverseAddress("0.0", "0");
        assertEquals(unLocated, info.getFormattedAddress());
        assertNull(info.getCity());

        info = AddressUtil.inverseAddress("128.481488", "39.990464");
        assertEquals(unLocated, info.getFormattedAddress());
        assertNull(info.getCity());

        info = AddressUtil.inverseAddress("128.481488", "");
        assertEquals(unLocated, info.getFormattedAddress());
        assertNull(info.getCity());

        info = AddressUtil.inverseAddress("", "");
        assertEquals(unLocated, info.getFormattedAddress());
        assertNull(info.getCity());

        info = AddressUtil.inverseAddress(null, "");
        assertEquals(unLocated, info.getFormattedAddress());
        assertNull(info.getCity());

        info = AddressUtil.inverseAddress((String) null, null);
        assertEquals(unLocated, info.getFormattedAddress());
        assertNull(info.getCity());
    }

    // @Test
    public void testBatchGetAddress() {
        final Map<String, String> result = AddressUtil.batchInverseAddress(new HashSet<>(Arrays.asList(
                "116.481488,39.990464",
                "128.481488,39.990464",
                "0.0,0.0",
                "0,0",
                "0,0.0",
                "0.0,0")));
        assertEquals(6, result.size());
        assertNotEquals(unknownLocation, result.get("116.481488,39.990464"));
        assertEquals(unLocated, result.get("128.481488,39.990464"));
        assertEquals(unLocated, result.get("0.0,0.0"));
        assertEquals(unLocated, result.get("0,0"));
        assertEquals(unLocated, result.get("0,0.0"));
        assertEquals(unLocated, result.get("0.0,0"));
    }

    // @Test
    public void testDistance() {
        double distance = AddressUtil.getDistance(116.368904, 39.923423, 116.387271, 39.922501);
        assertEquals(1571.0, distance, 1.0);
    }

    // @Test
    public void testMultiThread() throws InterruptedException {
        LongAdder threadCompleted = new LongAdder();

        Thread[] threads = new Thread[40];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                PositionInfo info = AddressUtil.inverseAddress("116.481488", "39.990464");
                assertTrue(info.getFormattedAddress().startsWith("北京市朝阳区望京街道"));
                threadCompleted.increment();
            });
        }
        for (int i = 0; i < 10; i++) {
            threads[10 + i] = new Thread(() -> {
                PositionInfo info = AddressUtil.inverseAddress("128.481488", "39.990464");
                assertEquals(unLocated, info.getFormattedAddress());
                assertNull(info.getCity());
                threadCompleted.increment();
            });
        }
        for (int i = 0; i < 10; i++) {
            threads[20 + i] = new Thread(() -> {
                PositionInfo info = AddressUtil.inverseAddress("106.512359", "29.535564");
                assertTrue(info.getFormattedAddress().startsWith("重庆市渝中区石油路街道"));
                threadCompleted.increment();
            });
        }
        for (int i = 0; i < 10; i++) {
            threads[30 + i] = new Thread(() -> {
                PositionInfo info = AddressUtil.inverseAddress("106.530257", "29.452519");
                assertTrue(info.getFormattedAddress().startsWith("重庆市巴南区花溪街道"));
                threadCompleted.increment();
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        assertEquals(threads.length, threadCompleted.intValue());
    }
}
