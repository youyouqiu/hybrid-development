package com.zw.platform.domain.share;

import com.alibaba.fastjson.JSONObject;

/**
 * <p>
 * Title:自动上传时间Util
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
public class UploadTimeUtil {
    static JSONObject map = new JSONObject();
    static {
        map = new JSONObject();
        map.put("被动",1);
        map.put("10",2);
        map.put("20",3);
        map.put("30",4);
    }


    public  static JSONObject getMap(){
        return map;
    }

    /**
     * 根据自动上传时间中文名获取对应编号  默认为3-无校验
     * @param uploadTimeStr  自动上传时间中文名
     * @return
     */
    public static  Integer getUploadTime(String uploadTimeStr){
        if (map.containsKey(uploadTimeStr))
            return map.getInteger(uploadTimeStr);
        return null;
    }

    /**
     * 根据自动上传时间值获取滤波系数中文名  默认为2-平滑
     * @param uploadTime  自动上传时间值
     * @return
     */
    public static  String getUploadTimeVal(Integer uploadTime){
        String key = null;
        //Map,HashMap并没有实现Iteratable接口.不能用于增强for循环.
        for(String getKey: map.keySet()){
            if(map.get(getKey).equals(uploadTime)){
                key = getKey;
                return key;
            }
        }
        return null;
    }
}
