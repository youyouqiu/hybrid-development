package com.zw.platform.service.sendTxt;

import com.zw.platform.domain.vas.monitoring.form.T808_0x8202;
import com.zw.ws.entity.t808.parameter.ParamItem;

import java.util.List;

/**
 * Created by LiaoYuecai on 2017/3/31.
 */
public interface F3SendTxtService {


    /**
     * 参数设置下发, 下发8103
     * @param vehicleId     车id
     * @param parameterName 设置id
     * @param paramType 下发参数类型
     * @param isOvertime 是否进行超时  true 超时 false 不超过
     *
     */
    String setF3SetParam(String vehicleId, String parameterName, List<ParamItem> params, String paramType,
        boolean isOvertime);
    
    /**
     * 下发8202
     */
    String setParametersTrace(String vehicleId, String parameterName, T808_0x8202 t8080x8202, String paramType,
        boolean isOvertime);
}
