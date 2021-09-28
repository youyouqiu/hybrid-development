package com.cb.platform.util.page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * pass层接口结果处理工具
 *
 * @author lijie
 * @version 1.0
 * @date 2020/9/16 9:47
 */
public class PassCloudResultUtil {

    private static final Logger logger = LogManager.getLogger(com.cb.platform.util.page.PassCloudResultUtil.class);

    public static <T> List<T> getListResult(String re, Class<T> clazz) {
        PassCloudResultBean passCloudResultBean = PassCloudResultBean.getDataInstance(re);
        List<T> list = new ArrayList<>();
        Object o = passCloudResultBean.getData();
        if (!passCloudResultBean.isSuccess()) {
            logger.info("调用 pass api 失败！" + re);
        } else {
            list = JSONObject.parseArray(o.toString(), clazz);
        }
        return list;
    }

    public static <T> List<T> getListResult(String re, TypeReference<List<T>> type) {
        PassCloudResultBean passCloudResultBean = PassCloudResultBean.getDataInstance(re);
        Object o = passCloudResultBean.getData();
        if (!passCloudResultBean.isSuccess()) {
            logger.info("调用 pass api 失败！" + re);
            return Collections.emptyList();
        }
        return JSON.parseObject(o.toString(), type);
    }

    public static <T> T getClassResult(String re, Class<T> c) {
        PassCloudResultBean passCloudResultBean = PassCloudResultBean.getDataInstance(re);
        T obj = null;
        Object o = passCloudResultBean.getData();
        if (!passCloudResultBean.isSuccess()) {
            logger.info("调用 pass api 失败！" + re);
        } else {
            obj = JSONObject.parseObject(o.toString(), c);
        }
        return obj;
    }


}


