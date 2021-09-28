package com.zw.platform.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.vas.carbonmgt.TimingStored;
import com.zw.platform.repository.core.TimingStoredDao;
import com.zw.platform.util.common.Converter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 燃料价格存储 Created by wjy on 2017/2/15.
 * @deprecated api已失效，而且存在socket泄漏问题
 */
@DisallowConcurrentExecution
public class TimingStoredJob implements Job {
    private static Logger log = LogManager.getLogger(TimingStoredJob.class);

    private TimingStoredDao timingStoredDao;

    @Value("${oil.price.key}")
    private String priceKey;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ApplicationContext appCtx = null;
        try {
            appCtx = (ApplicationContext) jobExecutionContext.getScheduler().getContext().get("applicationContextKey");
        } catch (SchedulerException e1) {
            log.error("燃料价格存储报错1" + e1);
        }
        if (appCtx != null) {
            timingStoredDao = appCtx.getBean(TimingStoredDao.class);
            String time = Converter.toString(new Date(), "yyyy-MM-dd");
            List<TimingStored> list = timingStoredDao.list(time);
            if (list == null || list.size() <= 0) {
                String[] citys =
                    new String[] { "北京", "上海", "天津", "重庆", "黑龙江", "吉林", "辽宁", "河北", "河南", "山东", "山西", "陕西", "甘肃", "青海",
                        "海南", "贵州", "湖北", "湖南", "浙江", "江苏", "福建", "安徽", "四川", "江西", "广东", "宁夏", "新疆", "内蒙古", "广西",
                        "西藏" };
                if (StringUtils.isBlank(priceKey)) {
                    priceKey = "83318125b2714220bb46d64d2534f1ba";
                }
                for (String str1 : citys) {
                    URL u = null;
                    InputStream in = null;
                    try {
                        u = new URL("http://route.showapi.com/138-46?showapi_appid=42082&prov=" + URLEncoder
                            .encode(str1, "utf-8") + "&showapi_sign=" + priceKey);
                        in = u.openStream();
                    } catch (MalformedURLException e) {
                        log.error("燃料价格存储报错2" + e);
                    } catch (IOException e) {
                        log.error("燃料价格存储报错3" + e);
                    }
                    log.info("请求 " + str1 + " 油价");
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    String uv = "";
                    try {
                        byte[] buf = new byte[1024];
                        int read = 0;
                        if (in != null) {
                            while ((read = in.read(buf)) > 0) {
                                out.write(buf, 0, read);
                            }
                        }
                    } catch (IOException e) {
                        log.error("燃料价格存储报错4" + e);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                log.error("燃料价格存储报错5" + e);
                            }
                        }
                    }
                    byte[] b = out.toByteArray();
                    try {
                        uv = new String(b, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        log.error("燃料价格存储报错6" + e);
                    }
                    log.info("第三方响应 " + str1 + " 油价请求 " + uv);
                    JSONObject json = null;
                    JSONObject jsonOne = null;
                    try {
                        json = JSONObject.parseObject(uv);
                        JSONObject jsonList = (JSONObject) json.get("showapi_res_body");
                        JSONArray jsonOil = (JSONArray) jsonList.get("list");
                        if (jsonOil != null && jsonOil.size() > 0) {
                            jsonOne = (JSONObject) jsonOil.get(0);
                            timingStoredDao.add(UUID.randomUUID().toString(), (String) jsonOne.get("prov"), "0",
                                (String) jsonOne.get("p0"), time);
                            timingStoredDao.add(UUID.randomUUID().toString(), (String) jsonOne.get("prov"), "92",
                                (String) jsonOne.get("p92"), time);
                            timingStoredDao.add(UUID.randomUUID().toString(), (String) jsonOne.get("prov"), "95",
                                (String) jsonOne.get("p95"), time);
                            timingStoredDao.add(UUID.randomUUID().toString(), (String) jsonOne.get("prov"), "97",
                                (String) jsonOne.get("p97"), time);
                        }
                    } catch (Exception e) {
                        if (json != null) {
                            log.error("燃料价格存储报错sql" + e + "**" + jsonOne.toString() + "**" + json.toString());
                        }
                    }
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        log.error("燃料价格存储报错睡眠" + e);
                    }
                }
            }

        }
    }
}
