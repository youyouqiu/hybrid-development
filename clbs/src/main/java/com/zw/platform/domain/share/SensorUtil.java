package com.zw.platform.domain.share;

import com.alibaba.fastjson.JSONObject;

/**
 * 传感器工具类
 * @author hujun
 */
public class SensorUtil {
	
	static JSONObject map = new JSONObject();
    static {
        map = new JSONObject();
        map.put("温度传感器1",21);
        map.put("温度传感器2",22);
        map.put("温度传感器3",23);
        map.put("温度传感器4",24);
        map.put("温度传感器5",25);
        map.put("湿度传感器1",26);
        map.put("湿度传感器2",27);
        map.put("湿度传感器3",28);
        map.put("湿度传感器4",29);
    }
	
    public  static JSONObject getMap(){
        return map;
    }

    /**
     * 根据传感器类别中文名获得对应数字
     * @param sensorOutName  传感器类别中文名
     * @return
     */
    public static  Integer getSensor(String sensorOutName){
        if (map.containsKey(sensorOutName)){
        	return map.getInteger(sensorOutName);
        }
        return null;
    }

    /**
     * 根据传感器类型数字得到对应的中文名
     * @param sensorTypeNumber  传感器类别数字
     * @return
     */
    public static  String getSensorOutName(Integer sensorTypeNumber){
        String key = null;
        for(String getKey: map.keySet()){
            if(map.get(getKey).equals(sensorTypeNumber)){
                key = getKey;
                return key;
            }
        }
        return null;
    }
}
