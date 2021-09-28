package com.zw.ws.entity.adas.paramSetting;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 主动安全中位标准参数下发工具类
 * @Author zhangqiang
 * @Date 2020/5/8 14:41
 */
public class ZhongWeiParamSettingUtil {

    /**
     * 使能顺序
     */
    public static Map<String, Integer> enableOrderMap = new HashMap();

    /**
     * 使能顺序
     */
    public static Map<String, Integer> auxiliaryEnableOrderMap = new HashMap();

    /**
     * 事件参数交互字段维护
     */
    public static Map<String, String> assistAlarmParamMap = new HashMap();
}
