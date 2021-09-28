package com.zw.platform.commons;

import org.junit.BeforeClass;
import org.junit.Test;

public class HttpClientUtilTest {

    private static final String ES_URL = "http://192.168.24.127:9200/_sql";

    @BeforeClass
    public static void setUp() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");
    }

    @Test
    public void testElasticSqlQuery() throws InterruptedException {
        // Thread thread1 = new Thread(() -> {
        //     JSONObject obj = HttpClientUtil.doHttPost(ES_URL, "select count(*) from adas_risk");
        //     assertNotNull(obj);
        //     JSONObject hits = obj.getJSONObject("hits");
        //     assertNotNull(hits);
        //     int total = hits.getIntValue("total");
        //     assertTrue(total >= 0);
        // }, "thread-1");
        //
        // Thread thread2 = new Thread(() -> {
        //     JSONObject obj = HttpClientUtil.doHttPost(ES_URL, "select count(*) from adas_risk_event");
        //     assertNotNull(obj);
        //     JSONObject hits = obj.getJSONObject("hits");
        //     assertNotNull(hits);
        //     int total = hits.getIntValue("total");
        //     assertTrue(total >= 0);
        // }, "thread-2");
        // thread1.start();
        // thread2.start();
        //
        // thread1.join();
        // thread2.join();
    }
}
