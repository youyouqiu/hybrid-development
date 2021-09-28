package com.zw.platform.domain.share;

import com.alibaba.fastjson.JSONObject;

/**
 * <p>
 * Title:补偿使能Util
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月17日 9:00
 */
public class CompEnUtil {
    static JSONObject map = new JSONObject();
    static {
        map = new JSONObject();
        map.put("使能",1);
        map.put("禁用",2);
    }

    public  static JSONObject getMap(){
        return map;
    }

    /**
     * 根据补偿使能中文名获取对应编号  默认为3-无校验
     * @param compEnStr  补偿使能中文名
     * @return
     */
    public static  Integer getCompEn(String compEnStr){
        if (map.containsKey(compEnStr))
            return map.getInteger(compEnStr);
        return null;
    }

    /**
     * 根据补偿使能值获取滤波系数中文名  默认为2-平滑
     * @param compEn  补偿使能值
     * @return
     */
    public static  String getCompEnVal(Integer compEn){
        String key = null;
        //Map,HashMap并没有实现Iteratable接口.不能用于增强for循环.
        for(String getKey: map.keySet()){
            if(map.get(getKey).equals(compEn)){
                key = getKey;
                return key;
            }
        }
        return null;
    }
}
