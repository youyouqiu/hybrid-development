package com.zw.adas.domain.define.setting.dto;

import com.zw.adas.domain.define.setting.AdasPlatformParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.platform.util.common.BusinessException;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: zjc
 * @Description:主动安全前端传递参数接收实体
 * @Date: create in 2020/12/4 15:54
 */
@Data
public class AdasParamRequestDTO {
    /**
     * 车辆id，多个按照逗号隔开
     */
    private String vehicleIds;
    /**
     * 主动安全设置参数json串
     */
    private String alarmParam;
    /**
     * 主动安全平台设置参数json串
     */
    private String platformParam;
    /**
     * 是否进行参数下发标志(true下发 false不下发)
     */
    private boolean sendFlag;

    /**
     * --------------------------------------
     **/

    /**
     * 车辆id集合
     */
    private List<String> vehicleIdList;
    /**
     * 主动安全设置参数集合
     */
    private List<AdasParamSettingForm> alarmParamSettingList;
    /**
     * 主动安全平台设置参数集合
     */
    private List<AdasPlatformParamSetting> platformParamSettingList;

    public void init() {
        vehicleIdList = Arrays.asList(vehicleIds.split(","));
        alarmParamSettingList = AdasParamSettingForm.convertList(alarmParam);
        platformParamSettingList = AdasPlatformParamSetting.convertList(platformParam);
    }

    public void checkVehIdIsEmpty() throws BusinessException {
        String[] split = vehicleIds.split(",");
        if (split.length == 0) {
            throw new BusinessException("参数错误，车辆id不能为空！");
        }
    }

    /**
     * 是否满足下发的条件
     * @return
     */
    public boolean canSendParam() {
        return sendFlag && alarmParamSettingList.size() > 0;
    }

    /**
     * 获取主动安全设置中的协议类型
     * @return
     */
    public Integer getProtocolType() {
        if (CollectionUtils.isEmpty(alarmParamSettingList)) {
            return null;
        }
        return alarmParamSettingList.get(0).getCommonParamSetting().getProtocolType();
    }
}
