package com.zw.platform.domain.share;

import com.alibaba.fastjson.JSONObject;

/**
 * <p>
 * Title:奇偶校验Util
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月17日 8:54
 */
public class ParityCheckUtil {


    static JSONObject map = new JSONObject();
    static {
        map = new JSONObject();
        map.put("奇校验",1);
        map.put("偶校验",2);
        map.put("无校验",3);
    }


    public  static JSONObject getMap(){
        return map;
    }

    /**
     * 根据滤奇偶校验中文名获取对应编号  默认为3-无校验
     * @param parityCheckStr  奇偶校验中文名
     * @return
     */
    public static Integer getParityCheck(String parityCheckStr){
        if (map.containsKey(parityCheckStr))
            return map.getInteger(parityCheckStr);
        return null;
    }

    /**
     * 根据奇偶校验值获取滤波系数中文名  默认为2-平滑
     * @param parityCheck  奇偶校验值
     * @return
     */
    public static String getParityCheckVal(Integer parityCheck){
        String key = null;
        //Map,HashMap并没有实现Iteratable接口.不能用于增强for循环.
        for(String getKey: map.keySet()){
            if(map.get(getKey).equals(parityCheck)){
                key = getKey;
                return key;
            }
        }
        return null;
    }
}
