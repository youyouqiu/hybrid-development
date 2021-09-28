package com.zw.adas.domain.define.setting.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.AdasCommonParamSetting;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/***
 @Author gfw
 @Date 2019/6/10 10:57
 @Description 新增参数设置(通用 / 报警)
 @version 1.0
 **/
@Data
public class AdasParamSettingForm implements Serializable {
    private static final long serialVersionUID = 1L;

    private String vehicleId;

    /**
     * 通用参数设置
     */
    private AdasCommonParamSetting commonParamSetting;

    /**
     * 报警参数设置
     */
    private List<AdasAlarmParamSetting> adasAlarmParamSettings;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AdasParamSettingForm that = (AdasParamSettingForm) o;
        return Objects.equals(vehicleId, that.vehicleId) && commonParamSetting.getProtocolType()
            .equals(that.commonParamSetting.getProtocolType()) && commonParamSetting.getParamType()
            .equals(that.commonParamSetting.getParamType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, commonParamSetting.getProtocolType(), commonParamSetting.getParamType());
    }

    /**
     * 转换前端传递的参数
     */
    public static List<AdasParamSettingForm> convertList(String alarmParam) {
        JSONArray array = JSON.parseArray(alarmParam);
        List<AdasParamSettingForm> adasParamSettingForms = new ArrayList<>();
        if (array == null) {
            return adasParamSettingForms;
        }
        for (Object o : array) {
            AdasParamSettingForm adasParamSettingForm =
                JSON.parseObject(JSON.toJSONString(o), AdasParamSettingForm.class);
            adasParamSettingForms.add(adasParamSettingForm);
        }
        return adasParamSettingForms;
    }
}
