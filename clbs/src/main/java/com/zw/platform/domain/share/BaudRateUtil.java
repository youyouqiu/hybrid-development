package com.zw.platform.domain.share;

import com.alibaba.fastjson.JSONObject;

/**
 * <p>
 * Title:波特率Util
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
public class BaudRateUtil {

    static JSONObject map = new JSONObject();
    static {
        map = new JSONObject();
        map.put("2400",1);
        map.put("4800",2);
        map.put("9600",3);
        map.put("19200",4);
        map.put("38400",5);
        map.put("57600",6);
        map.put("115200",7);
    }

    public  static JSONObject getMap(){
        return map;
    }

    /**
     * 根据波特率中文名获取对应编号  默认为3-无校验
     * @param baudRateStr  波特率中文名
     * @return
     */
    public static  Integer getBaudRate(String baudRateStr){
        if (map.containsKey(baudRateStr))
            return map.getInteger(baudRateStr);
        return null;
    }

    /**
     * 根据波特率值获取滤波系数中文名  默认为2-平滑
     * @param baudRate  波特率值
     * @return
     */
    public static  String getBaudRateVal(Integer baudRate){
        String key = null;
        //Map,HashMap并没有实现Iteratable接口.不能用于增强for循环.
        for(String getKey: map.keySet()){
            if(map.get(getKey).equals(baudRate)){
                key = getKey;
                return key;
            }
        }
        return null;
    }
}
