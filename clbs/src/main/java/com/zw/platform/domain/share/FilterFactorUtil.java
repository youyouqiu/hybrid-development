package com.zw.platform.domain.share;

import com.alibaba.fastjson.JSONObject;

/**
 * <p>
 * Title:滤波系数 通用
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月17日 8:48
 */
public class FilterFactorUtil {


    static JSONObject map = new JSONObject();
    static {
        map = new JSONObject();
        map.put("实时",1);
        map.put("平滑",2);
        map.put("平稳",3);
    }


    public  static JSONObject getMap(){
        return map;
    }
    /**
     * 根据滤波系数中文名获取对应编号  默认为2-平滑
     * @param filterFactorStr  滤波系数 中文名
     * @return
     */
    public static  Integer getFilterFactor(String filterFactorStr){
        if (map.containsKey(filterFactorStr))
            return map.getInteger(filterFactorStr);
        return null;
    }

    /**
     * 根据滤波系数值获取滤波系数中文名  默认为2-平滑
     * @param filterFactor  滤波系数值
     * @return
     */
    public static  String getFilterFactorVal(Integer filterFactor){
      String key = null;
      //Map,HashMap并没有实现Iteratable接口.不能用于增强for循环.
      for(String getKey: map.keySet()){
        if(map.get(getKey).equals(filterFactor)){
             key = getKey;
            return key;
        }
      }
      return null;
    }
}
