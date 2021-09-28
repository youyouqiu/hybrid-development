package com.zw.talkback.util.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.util.StrUtil;

/**
 * 数据组装参数行数
 * @param <D>
 */
@FunctionalInterface
public interface AssembleFunction<D> {
    D asssemble(String t);

    /**
     * 转换JSONArray
     * @param str
     * @return
     */
    static JSONArray assembleArray(String str) {
        if (StrUtil.isBlank(str)) {
            return null;
        }
        return JSONObject.parseArray(str);
    }

    /**
     * 转JSONObject
     * @param str
     * @return
     */
    static JSONObject assembleObj(String str) {
        if (StrUtil.isBlank(str)) {
            return null;
        }
        return JSONObject.parseObject(str);
    }
}
