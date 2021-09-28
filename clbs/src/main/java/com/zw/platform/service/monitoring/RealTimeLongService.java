package com.zw.platform.service.monitoring;

import com.zw.platform.domain.multimedia.form.LongOrderForm;
import com.zw.platform.domain.param.StationParam;
import com.zw.ws.entity.aso.ASOFixedPoint;
import com.zw.ws.entity.aso.ASOFrequency;
import com.zw.ws.entity.aso.ASOTransparent;

public interface RealTimeLongService {
	/**
	 * F3超长待机上报频率设置
	 * @param vehicleId
	 * @param stationParam
	 * @return
	 * @throws Exception
	 */
    boolean sendReportSet(String vehicleId,StationParam stationParam) throws Exception;
    /**
     * F3超长待机位置跟踪
     * @param form
     * @return
     * @throws Exception
     */
    boolean sendLocationTracking(LongOrderForm form) throws Exception;
    /**
     * 艾赛欧超长待机上报频率设置
     * @param vehicleId
     * @param stationParam
     * @return
     * @throws Exception
     */
    boolean sendASOReportSet(String vehicleId,ASOFrequency frequency) throws Exception;
    /**
     * 艾赛欧超长待机定点上报设置
     * @param vehicleId
     * @param stationParam
     * @return
     * @throws Exception
     */
    boolean sendASOFixedPoint(String vehicleId,ASOFixedPoint fixedPoint) throws Exception;
    /**
     * 艾赛欧超长待机透传指令
     * @param vehicleId
     * @param asoTransparent
     * @return
     * @throws Exception
     */
    boolean sendPassthroughInstruction(String vehicleId,ASOTransparent asoTransparent) throws Exception;
    /**
     * 艾赛欧复位重启
     * @param vehicleId
     * @return
     * @throws Exception
     */
    boolean sendRestart(String vehicleId) throws Exception;
}
