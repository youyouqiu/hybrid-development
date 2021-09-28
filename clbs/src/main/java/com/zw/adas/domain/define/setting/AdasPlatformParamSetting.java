package com.zw.adas.domain.define.setting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdasPlatformParamSetting implements Serializable {

    /**
     * 车辆id
     **/
    private String vehicleId;

    /**
     * 风险事件(function_id)
     */
    private String riskFunctionId;

    /**
     * 报警提醒方式（一级报警） 0无,1闪烁,2提示音,3闪烁加提示音,4弹窗提醒,5短信提醒
     */
    private Byte alarmRemindOne;

    /**
     * 报警提醒方式（二级报警） 0无,1闪烁,2提示音,3闪烁加提示音,4弹窗提醒,5短信提醒
     */
    private Byte alarmRemindTwo;

    /**
     * 处理间隔时间（秒）（一级报警）
     */
    private Integer processingIntervalOne;

    /**
     * 处理间隔时间（秒）（二级报警）
     */
    private Integer processingIntervalTwo;

    /**
     * 时间阈值（秒）
     */
    private Integer timeThreshold;

    /**
     * 时间报警数量阈值
     */
    private Integer timeAlarmNumThreshold;

    /**
     * 时间阈值报警提醒方式（一级报警） 0无,1闪烁,2提示音,3闪烁加提示音,4弹窗提醒,5短信提醒
     */
    private Byte timeAlarmRemind;

    /**
     * 距离阈值(km)
     */
    private Integer distanceThreshold;

    /**
     * 距离报警数量阈值
     */
    private Integer distanceAlarmNumThreshold;

    /**
     * 距离阈值报警提醒方式 0无,1闪烁,2提示音,3闪烁加提示音,4弹窗提醒,5短信提醒
     */
    private Byte distanceAlarmRemind;

    /**
     * 报警开关（0关，1开）
     */
    private Byte alarmSwitch;

    /**
     * 页签
     */
    private Byte paramType;

    private Byte flag = 1;
    //*******************************************京标分割线*********************************//

    /**
     * 报警提醒方式（三级报警） 0无,1闪烁,2提示音,3闪烁加提示音,4弹窗提醒,5短信提醒
     */
    private Byte alarmRemindThree;

    /**
     * 处理间隔时间（秒） （三级报警）
     */
    private Integer processingIntervalThree;

    /**
     * 自动获取附件 一级
     */
    private Byte automaticGetOne;
    /**
     * 自动获取附件 二级
     */
    private Byte automaticGetTwo;
    /**
     * 自动获取附件 三级
     */
    private Byte automaticGetThree;

    /**
     * 自动处理 一级
     */
    private Byte automaticDealOne;
    /**
     * 自动处理 二级
     */
    private Byte automaticDealTwo;
    /**
     * 自动处理 三级
     */
    private Byte automaticDealThree;

    public static List<AdasPlatformParamSetting> convertList(String platformParam) {
        JSONArray array = JSON.parseArray(platformParam);
        List<AdasPlatformParamSetting> adasParamSettingForms = new ArrayList<>();
        if (array == null) {
            return adasParamSettingForms;
        }
        for (Object o : array) {
            AdasPlatformParamSetting adasParamSettingForm =
                JSON.parseObject(JSON.toJSONString(o), AdasPlatformParamSetting.class);
            adasParamSettingForms.add(adasParamSettingForm);
        }
        return adasParamSettingForms;
    }

}
