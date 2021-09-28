package com.zw.platform.util;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Map;

/***
 * 集合工具类
 * 2019/5/24 11:33
 *
 * @author zhengjc
 * @version 1.0
 **/
public class CosUtil {

    /**
     * 判断多个参数同时不为空才成立
     */
    public static boolean areNotEmpty(Collection<?>... checkDatas) {
        boolean result = true;
        for (Collection<?> checkData : checkDatas) {
            result = CollectionUtils.isNotEmpty(checkData);
            if (!result) {
                break;
            }

        }
        return result;
    }

    /**
     * 获取map一个值
     */
    public static <T, R> R getOneDataFromMap(Map<T, R> map) {
        return getFirstData(map.values());
    }

    /**
     * 获取集合的第一个值
     */
    public static <T> T getFirstData(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        return collection.iterator().next();
    }

}
