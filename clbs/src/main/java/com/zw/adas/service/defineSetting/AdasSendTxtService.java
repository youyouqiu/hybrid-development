package com.zw.adas.service.defineSetting;

import com.zw.ws.entity.t808.parameter.ParamItem;

import java.util.List;


/**
 * Created by LiaoYuecai on 2017/3/31.
 */
public interface AdasSendTxtService {

    /**
     * 下发8103
     *
     * @param vehicleId     车id
     * @param parameterName 设置id
     * @param paramType     下发参数类型
     * @param isOvertime    是否进行超时  true 超时 false 不超过
     * @Description: 参数设置下发
     */
    String sendF3SetParam(String vehicleId, String parameterName, List<ParamItem> params, String paramType,
        boolean isOvertime, String userName) throws Exception;

}
