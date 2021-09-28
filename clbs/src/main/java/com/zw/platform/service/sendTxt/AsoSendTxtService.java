package com.zw.platform.service.sendTxt;

import com.zw.ws.entity.aso.ASOFixedPoint;
import com.zw.ws.entity.aso.ASOFrequency;
import com.zw.ws.entity.aso.ASOTransparent;

/**
 * <p>
 * Title:艾赛欧参数下发
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author nixiangqian
 * @since 2017年07月28日 14:06
 */
public interface AsoSendTxtService {

    /**
     * 定点下发
     */
    void sendFixedPoint(String vehicleId, ASOFixedPoint fixedPoint);

    /**
     * 上传频率
     */
    void sendFrequency(String vehicleId, ASOFrequency frequency);

    /**
     * 透传命令
     */
    void sendTransparent(String vehicleId, ASOTransparent transparent);

    /**
     * 复位重启
     */
    void sendRestart(String vehicleId);

}
