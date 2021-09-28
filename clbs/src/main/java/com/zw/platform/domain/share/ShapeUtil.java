package com.zw.platform.domain.share;

import com.alibaba.fastjson.JSONObject;

/**
 * <p>
 * Title:油箱形状Util
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月23日 10:19
 */
public class ShapeUtil {

    static JSONObject map = new JSONObject();
    static {
        map = new JSONObject();
        map.put("长方形",1);
        map.put("圆柱形",2);
        map.put("D形",3);
        map.put("椭圆形",4);
        map.put("其他",5);
    }

    public  static JSONObject getMap(){
        return map;
    }

    /**
     * 根据油箱形状中文名获取对应编号  默认为3-无校验
     * @param shapeStr  油箱形状中文名
     * @return
     */
    public static  Integer getShape(String shapeStr){
        if (map.containsKey(shapeStr))
            return map.getInteger(shapeStr);
        return null;
    }

    /**
     * 根据油箱形状值获取滤波系数中文名  默认为2-平滑
     * @param shape  油箱形状值
     * @return
     */
    public static  String getShapeVal(Integer shape){
        String key = null;
        //Map,HashMap并没有实现Iteratable接口.不能用于增强for循环.
        for(String getKey: map.keySet()){
            if(map.get(getKey).equals(shape)){
                key = getKey;
                return key;
            }
        }
        return null;
    }
}
