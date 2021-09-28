package com.zw.platform.service.switching;

import com.zw.platform.domain.vas.switching.SwitchSignalStatisticsInfo;
import com.zw.platform.domain.vas.switching.query.SwitchSignalQuery;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/9/6 16:38
 */
public interface SwitchSignalStatisticsService {

    /**
     * 获得绑定了开关信号的车辆
     *
     * @return
     * @throws Exception
     */
    List<SwitchSignalStatisticsInfo> getBindSwitchSignalVehicle() throws Exception;

    /**
     * 获取开关信号报表图表信息
     *
     * @param query
     * @return
     * @throws Exception
     */
    JsonResultBean getSwitchSignalChartInfo(SwitchSignalQuery query) throws Exception;


    /**
     * 获得开关信号报表终端表格信息
     *
     * @param query
     * @return
     * @throws Exception
     */
    PageGridBean getSwitchSignalTerminalFormInfo(SwitchSignalQuery query) throws Exception;

    /**
     * 获得开关信号报表采集板1表格信息
     *
     * @param query
     * @return
     * @throws Exception
     */
    PageGridBean getSwitchSignalAcquisitionBoardOneFormInfo(SwitchSignalQuery query) throws Exception;

    /**
     * 获得开关信号报表采集板2表格信息
     *
     * @param query
     * @return
     * @throws Exception
     */
    PageGridBean getSwitchSignalAcquisitionBoardTwoFormInfo(SwitchSignalQuery query) throws Exception;
}
